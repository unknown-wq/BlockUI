# BlockUI → Fabric / Minecraft 26.2

Рабочий репозиторий порта. В **`26.1.2/`** лежит исходный код из ветки апстрима
`ldtteam/BlockUI@port/26` — **только код и ресурсы, для чтения, не редактируют**.
Сборочная обвязка апстрима удалена: собирать будем своим Gradle 9.6.1 под Java 25
в отдельной папке `26.2/`.

```
/
├── 26.1.2/         # исходник: MC 26.1.2, NeoForge 26.1.2.68-beta, Java 25, 109 java
├── porting-26.2/   # инструкции для оркестратора и агентов + скрипты массовых ренеймов
├── gradle-dist/    # вендоренный Gradle 9.6.1 (в контейнере его не скачать)
└── 26.2/           # (появится) сам порт на Fabric 26.2
```

Окружение ставится один раз:

```sh
./gradle-dist/install.sh                                  # → /opt/gradle-9.6.1
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
/opt/gradle-9.6.1/bin/gradle --version                    # должно быть 9.6.1
```

`./gradlew` в этом окружении не работает: прокси отдаёт 403 на ассеты GitHub-релизов.
Начинать с `porting-26.2/PORT-ANY-MOD-26.2.md`.

## Почему одна папка, а не две

У апстрима есть `port/26`, и здесь, в отличие от Domum Ornamentum, выбирать не пришлось:
**`port/26` строго содержит линию 1.21.1.** Проверено — голова `version/main`
(`78dae57`, 07.03.2026) является предком `port/26`, а коммитов, которые есть в
`version/main` и отсутствуют в `port/26`, **ноль**. Диффать нечего, старая линия не нужна.
Она всё равно остаётся в истории форка: это родитель первого коммита этой раскладки.

| | значение |
|---|---|
| Ветка | `port/26` @ `bc14ca5` (30.05.2026), коммит «finishing touches» |
| Minecraft | **26.1.2** (`minecraft_range=[26.1.2, 27)`) |
| NeoForge | **26.1.2.68-beta** (минимум `26.1.2`) |
| Java | **25** |
| modId / group / version | `blockui` / `com.ldtteam` / `0.0.1` |
| Объём | **109 java**, 292 КБ ресурсов, 5 XML-описаний интерфейса |

Порт на 26.1.2 у апстрима сделан чисто: 25 файлов используют
`net.minecraft.resources.Identifier`, а восемь оставшихся упоминаний `ResourceLocation` —
это **собственные имена BlockUI**, а не ванильный класс: метод `getXmlResourceLocation()`,
поле `xmlResourceLocation` и класс `OutOfJarResourceLocation extends Identifier`.
Ссылок на удалённый ванильный тип не осталось ни одной.

Это самая продвинутая база во всей связке: 26.1.2 против 26.1 у Domum Ornamentum и
1.21.1 у Structurize и MineColonies. Ванильная ось пройдена целиком, остаётся смена
лоадера и дельта 26.1.2 → 26.2.

## Зависимости: мод полностью автономен

**В `neoforge.mods.toml` объявлены только `neoforge` и `minecraft`.** Никаких модов.

И главное открытие этого репозитория:

> **`com.ldtteam.common` — не внешняя библиотека. Она физически лежит внутри BlockUI**,
> в `src/main/java/com/ldtteam/common/**` — 28 файлов: `network/` (`PlayMessageType`,
> `AbstractServerPlayMessage`, `AbstractClientPlayMessage`, дистрибьюторы),
> `fakelevel/` (`FakeLevel`, `FakeChunk`, `FakeLevelLightEngine`, `SingleBlockFakeLevel`),
> `codec/` (`XmlOps`, `XmlValue`, `Codecs`), `config/`, `language/`, `util/`.

Отсюда следует то, что переворачивает план всей связки: **36 файлов Structurize и
209 импортов MineColonies, которые тянут `com.ldtteam.common`, зависят на самом деле
от BlockUI**, а не от какой-то отдельной библиотеки, которую надо искать. BlockUI
у обоих и так объявлен обязательной зависимостью. То есть порт BlockUI закрывает
сразу две графы в их таблицах зависимостей.

Остальные импорты — то, что приезжает с игрой или JDK:

| Импорт | Вхождений | Что это |
|---|---:|---|
| `com.ldtteam.blockui` | 228 | сам мод |
| `com.mojang.serialization` | 25 | ваниль |
| `org.joml` | 24 | математика, приезжает с игрой |
| `org.jetbrains.annotations` | 24 | аннотации (+ 2 уже на `org.jspecify`) |
| `com.mojang.blaze3d` | 23 | рендер |
| `com.ldtteam.common` | 13 | **свой же код, см. выше** |
| `org.w3c.dom` / `org.xml.sax` | 11 | JDK — разбор XML-описаний интерфейса |
| slf4j, lwjgl/glfw, apache, fastutil, gson, netty, brigadier, datafixers | ~20 | ваниль/JDK |
| `org.junit` | 5 | тесты (`src/test`) |

**Вывод:** BlockUI портируется первым и в полной изоляции — внешних зависимостей у него
нет вообще.

## Что вычищено из `26.1.2/`

`build.gradle` состоял из одной строки
`apply from: 'https://raw.githubusercontent.com/ldtteam/OperaPublicaCreator/ng7/gradle/mod.gradle'`,
то есть весь build-скрипт качался из сети.

- **Сборка:** `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`,
  `gradlew.bat`, `gradle/`.
- **CI и процесс:** `.github/`, `CLA.md`, `CODE_OF_CONDUCT.md`.

Осталось: `src/` (`main` + `test`, 109 java и ресурсы), `LICENSE`, `README.md` самого
мода, `.gitattributes`.

## Место в связке

Порядок портирования по §2 плана — от листьев к потребителям:

```
BlockUI  ──┐
           ├──> Structurize ──> MineColonies
Domum Ornamentum ──────────────┘
```

BlockUI и Domum Ornamentum — листья, оба портируются независимо и параллельно.
Structurize ждёт BlockUI (14 файлов GUI + 36 файлов на `ldtteam.common`),
MineColonies ждёт всех (146 файлов GUI + 209 импортов `ldtteam.common` + 147 файлов
на Structurize).
