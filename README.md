Поисковой движок по русскоязычной википедии
---
Используется булев поиск.

При разработке использовались официальные дампы вики ферваля 2017 года, парсились программой wikiextractor. В Resource можно настроить пути считывания данных.
* IndexService строит индексацию (токенизация делается только на уровне капитализации).
* SearchService загружает в память словарь с необходимыми данными и запускает сервер с поисковой строкой и выводом результатов на страницу.

Парсер запросов написан с помощью javulator.
Выводятся первые 100 результатов. 

Поддерживаются операции:
* И : "word1 word2"
* ИЛИ : "word1||word2"
* НЕ : "!word1"

Пример запросов:
```
роза !цветок
((роза одуванчик)||герань) !цветок
титаник фильм 1997
```
