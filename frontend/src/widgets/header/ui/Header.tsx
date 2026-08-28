import { Box, Container, Group, Anchor, Flex } from '@mantine/core'
import { HeaderButtonGroup } from './HeaderButtonGroup'
import { HeaderNavigation } from './HeaderNavigation'
import { HeaderBurgerMenu } from './HeaderBurgerMenu'
import { Logo } from '@shared/ui/Logo'
import { Link } from 'react-router'
import { useMantineTheme } from '@mantine/core'

const DEFAULT_HEADER_HEIGHT = 64

/**
 * Главный компонент шапки сайта (Header).
 *
 * Объединяет логотип, основную навигацию, кнопки авторизации и мобильное бургер-меню.
 * Фиксируется в верхней части экрана (`sticky`) с эффектом размытия заднего плана (glassmorphism)
 * и адаптирует внутренние отступы контейнера под различные размеры экранов.
 *
 * **Важно:** Высота шапки динамически запрашивается из кастомного свойства `other.headerHeight`
 * глобальной темы Mantine для синхронизации со скриптами плавного скролла. Если свойство не определено в теме,
 * используется базовое значение {@link DEFAULT_HEADER_HEIGHT}.
 *
 * @returns Семантический элемент `header` с полной разметкой шапки приложения.
 */
export function Header() {
  const { other } = useMantineTheme()
  const headerHeight = other?.headerHeight ?? DEFAULT_HEADER_HEIGHT

  return (
    <Box
      id="header"
      component="header"
      pos="sticky"
      top={0}
      bg="rgba(255,255,255,0.85)"
      style={{
        zIndex: 201,
        backdropFilter: 'blur(8px)',
        borderBottom: '1px solid var(--mantine-color-gray-2)',
      }}
    >
      <Container size="lg" px={{ base: 12, sm: 24 }}>
        <Group h={headerHeight} justify="space-between">
          <Flex gap={{ base: 'xs', sm: 28 }}>
            <Anchor component={Link} to="/#" underline="never">
              <Logo variant="dark" hideTextOnMobile />
            </Anchor>
            <HeaderNavigation />
          </Flex>
          <Group>
            <HeaderButtonGroup />
            <HeaderBurgerMenu />
          </Group>
        </Group>
      </Container>
    </Box>
  )
}
