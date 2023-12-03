# Название проекта
ShareIt

# Описание проекта
Реализован бэкенд для шеринга вещей. Используются следующие сущности: пользователь, вещь, бронирование, заявка, комментарий. Для них реализованы стандартные операции CRUD. Имеются также дополнительные возможности.
- *Бронирование вещей.* Можно бронировать вещи и выводить списки бронирований для заданного клиента или владельца вещи. Предусмотрена возможность для владельца вещи подтвердить или отвергнуть запрашиваемую бронь.
- *Заявки на вещи*. Пользователи могут оставлять заявки на вещи, которых в данный момент нет. Можно получать списки заявок конкретного пользователя или всех (с постраничным выводом).   
- *Поиск вещей по образцу*. В базе ищутся все вещи, имя или описание которых содержит заданный образец. Данные выдаются постранично.
- *Комментарии*. Пользователи могут оставлять комментарии к вещам, которые они арендовали.

Проект состоит из двух модулей: gateway и server. Первый валидирует HTTP-запросы и в случае их корректности передает на обработку во второй, который реализует основную функциональность. Оба модуля работают в контейнерах.

Проект реализован на основе **SpringBoot**. Для доступа к данным используется **SpringData** (как запросные методы, так и запросы на JPQL). 

# Развертывание и системные требования
*Версия Java*: 11. 

*Используются*: стартеры SpringBoot, модуль SpringData, база данных PostgreSQL, библиотека lombok. 

*Развертывание*: 
- собрать проект командой **mvn clean package**;
- запустить **Docker**; 
- перейти в каталог проекта и вызвать команду **docker-compose up**.

Модуль gateway будет развернут по адресу *localhost*:8080, а модуль server - по адресу *localhost*:9090. Данные будут храниться в базе PostgreSQL, доступной по адресу *localhost*:6541. Эти настройки можно изменить, редактируя файлы **docker-compose.yml** и **application.properties**. 
