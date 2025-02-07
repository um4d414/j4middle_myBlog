# Учебный проект блога myBlog

## Функциональность:

- Создание поста
- Редактирование поста
- Удаление поста
- Поддержка изображений в постах
- Просмотр ленты постов
- Комментирование поста
- Редактирование комментария
- Удаление комментария

## Стэк:

- Java 21
- Spring Framework:
    - Spring-boot
    - Web-MVC
    - Spring-Data-JDBC
- H2
- Thymeleaf

## Установка:

- Создать и установить в системную переменную $MY_BLOG_UPLOADS_DIR директорию для хранения изображений
- Выполнить команду в директории проекта:
      - .\mvnw.cmd package (для Windows) или ./mvnm package (для MacOS / Linux)
      - java -jar .\target\myBlog-0.2.jar (для Windows) или java -jar ./target/myBlog-0.2.jar (для MacOS / Linux)
- Сервис будет доступен по адресу http://localhost:8080/posts