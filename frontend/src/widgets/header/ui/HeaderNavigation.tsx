import { Anchor, Flex } from '@mantine/core'
import { Link } from 'react-router'
import { links } from '../model/headerConfig'

/**
 * Компонент основной навигационной панели для десктопных экранов.
 *
 * Рендерит семантический тег `<nav>` со списком ссылок, импортируемых из конфигурации.
 * Панель автоматически скрывается на экранах меньше `sm` (мобильные устройства)
 * и динамически увеличивает отступы между элементами при переходе на брейкпоинт `md`.
 *
 * @returns Семантический элемент навигации со списком ссылок.
 */
export function HeaderNavigation() {
  return (
    <Flex
      component="nav"
      gap={{ base: 12, md: 22 }}
      align="center"
      visibleFrom="sm"
    >
      {links.map((link) => (
        <Anchor
          component={Link}
          to={link.link}
          c="rgb(62, 67, 77)"
          fz="sm"
          key={link.label}
        >
          {link.label}
        </Anchor>
      ))}
    </Flex>
  )
}
