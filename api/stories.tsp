import "@typespec/http";
import "@typespec/openapi";

using TypeSpec.Http;
using TypeSpec.OpenAPI;

namespace HexletCareer.Api.Stories;

@doc("Карточка истории успеха выпускника Хекслета")
model StoryDto {
  @doc("Уникальный идентификатор истории")
  id: int64;

  @doc("Имя выпускника Хекслета")
  authorName: string;

  @doc("Ссылка на аватарку/фотографию")
  avatarUrl: string;

  @doc("Компания, куда устроился работать выпускник")
  companyName: string;

  @doc("Полученный оффер (занимаемая должность)")
  offerPosition: string;

  @doc("Текст истории выпускника")
  text: string;

  @doc("Порядок ручной сортировки для отображения на лендинге")
  displayOrder: int32;
}

@doc("Универсальная обертка для пагинации ответов")
model PageDto<T> {
  @doc("Элементы текущей страницы")
  content: T[];

  @doc("Общее количество элементов")
  totalElements: int64;

  @doc("Общее количество страниц")
  totalPages: int32;

  @doc("Номер текущей страницы")
  number: int32;
}

@doc("Страница со списком историй успеха")
model StoryPageResponse extends PageDto<StoryDto> {}

@route("/v1/stories")
@tag("Stories")
interface StoriesOperations {
  @doc("Получить список опубликованных историй с пагинацией и сортировкой")
  @get
  @operationId("getStories")
  op getStories(
    @doc("Номер запрашиваемой страницы (0-based)")
    @query page?: int32 = 0,

    @doc("Количество элементов на одной странице")
    @query size?: int32 = 10,

    @doc("Сортировка в формате property,asc|desc")
    @query sort?: string[] = #["displayOrder,asc"]
  ): StoryPageResponse;
}

// АДМИНСКИЙ КОНТУР УПРАВЛЕНИЯ КОНТЕНТОМ (CRUD)
// Согласно ТЗ задачи #1137, операции администратора зафиксированы в контракте
// для проектирования схемы данных, но вынесены за скоуп текущей реализации.
// Реализация бэкенд-контроллеров и UI админки будет выполнена в рамках
// отдельной изолированной задачи.
//
// @doc("Данные для создания новой истории успеха")
// model CreateStoryDto {
//   @doc("Имя выпускника Хекслета")
//   authorName: string;
//
//   @doc("Ссылка на аватарку/фотографию")
//   avatarUrl: string;
//
//   @doc("Компания, куда устроился работать выпускник")
//   companyName: string;
//
//   @doc("Полученный оффер (занимаемая должность)")
//   offerPosition: string;
//
//   @doc("Текст истории выпускника")
//   text: string;
//
//   @doc("Порядок ручной сортировки для отображения на лендинге")
//   displayOrder?: int32 = 0;
//
//   @doc("Признак публикации истории")
//   isPublished?: boolean = false;
// }
//
// @doc("Данные для обновления существующей истории успеха")
// model UpdateStoryDto {
//   @doc("Имя выпускника Хекслета")
//   authorName?: string;
//
//   @doc("Ссылка на аватарку/фотографию")
//   avatarUrl?: string;
//
//   @doc("Компания, куда устроился работать выпускник")
//   companyName?: string;
//
//   @doc("Полученный оффер (занимаемая должность)")
//   offerPosition?: string;
//
//   @doc("Текст истории выпускника")
//   text?: string;
//
//   @doc("Порядок ручной сортировки для отображения на лендинге")
//   displayOrder?: int32;
//
//   @doc("Признак публикации истории")
//   isPublished?: boolean;
// }
//
//  @route("/v1/admin/stories")
//  @tag("Admin Stories")
//  interface AdminStoriesOperations {
//
//    @doc("Получить историю успеха по ID")
//    @get
//    op show(@path id: int64): StoryDto;
//
//    @doc("Создать новую историю успеха")
//    @post
//    op create(@body story: CreateStoryDto): StoryDto;
//
//    @doc("Обновить историю успеха по ID")
//    @put
//    op update(@path id: int64, @body story: UpdateStoryDto): StoryDto;
//
//    @doc("Удалить историю успеха по ID")
//    @delete
//    op delete(@path id: int64): void;
//  }

