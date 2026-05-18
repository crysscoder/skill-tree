<div align="center">

# SkillTree

![Release](https://img.shields.io/github/v/release/crysscoder/skill-tree?style=flat-square&label=release)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.20.1-2ea44f?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-required-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Issues](https://img.shields.io/github/issues/crysscoder/skill-tree?style=flat-square)

Paper-плагин с ветками навыков, заданиями и сохранением прогресса в MySQL.

[Release](https://github.com/crysscoder/skill-tree/releases/latest) · [Issues](https://github.com/crysscoder/skill-tree/issues) · [CodeAdapter](https://codeadapter.ru)

</div>

## Что делает

- открывает меню выбора ветки через `/skilltree`
- поддерживает ветки Warrior, Farmer и Alchemist
- выдаёт задания по выбранной ветке
- отслеживает убийства мобов, блоки, урон, зелья и другие события
- хранит прогресс игроков в MySQL
- читает задания из `config.yml`, а подключение к базе из `bd.yml`

## Версии

| Компонент | Версия |
| --- | --- |
| Plugin | `1.0.0` |
| Java | `17` |
| Paper | `1.20.1` |
| Paper API | `1.20.1-R0.1-SNAPSHOT` |

## Команды

- `/skilltree`
- `/skilltree start`
- `/skilltree info`
- `/skilltree addtask <игрок> <номер>`

## Permission

- `skilltree.admin`
- по умолчанию доступно `op`

## Сборка

```bash
./gradlew clean build
```

Для Windows:

```powershell
.\gradlew.bat clean build
```
