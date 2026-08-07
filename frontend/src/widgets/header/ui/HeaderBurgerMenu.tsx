import { Burger, Drawer, Anchor, Stack } from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { Link } from 'react-router'
import { links } from '../model/headerConfig'

/**
 * Компонент мобильного меню навигации («бургер»).
 *
 * Отображает кнопку-бургер, которая скрывается на экранах больше `sm`.
 * При клике открывает полноэкранное боковое меню (Drawer) со списком ссылок,
 * импортируемых из конфигурации заголовка. При клике на любую ссылку меню автоматически закрывается.
 *
 * @returns Элемент разметки интерфейса для мобильной навигации.
 */
export function HeaderBurgerMenu() {
  const [opened, { toggle }] = useDisclosure()
  return (
    <>
      <Burger
        opened={opened}
        onClick={toggle}
        aria-label="Показать меню"
        hiddenFrom="sm"
        color="rgb(62, 67, 77)"
      />
      <Drawer
        opened={opened}
        onClose={toggle}
        size="100%"
        position="left"
        transitionProps={{ transition: 'slide-right' }}
      >
        <Stack m={16} py={24} gap={32}>
          {links.map((link) => (
            <Anchor
              component={Link}
              to={link.link}
              c="rgb(62, 67, 77)"
              fz="sm"
              onClick={toggle}
              key={link.label}
            >
              {link.label}
            </Anchor>
          ))}
        </Stack>
      </Drawer>
    </>
  )
}
