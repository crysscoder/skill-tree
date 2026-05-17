# SkillTree

Paper-плагин с ветками навыков, заданиями и сохранением прогресса в MySQL.

## Что делает

- открывает меню выбора ветки через `/skilltree`
- поддерживает ветки Warrior, Farmer и Alchemist
- выдаёт задания по выбранной ветке
- отслеживает убийства мобов, блоки, урон, зелья и другие события
- хранит прогресс игроков в MySQL
- читает задания из `config.yml`, а подключение к базе из `bd.yml`

## Версии

- плагин: `1.0.0`
- сервер: Paper `1.20.1`
- API: `1.20.1-R0.1-SNAPSHOT`
- Java: `17`

## Команды

- `/skilltree`
- `/skilltree start`
- `/skilltree info`
- `/skilltree addtask <игрок> <номер>`

## Permission

- `skilltree.admin`
- по умолчанию доступно op

## Сборка

```bash
./gradlew clean build
```

Для Windows:

```powershell
.\gradlew.bat clean build
```
