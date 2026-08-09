import { Group, ThemeIcon, Text, Stack, rem } from '@mantine/core'

/**
 * Свойства для настройки отображения логотипа.
 */
interface LogoProps {
  /**
   * Вариант цветовой схемы для текста логотипа.
   * Управляет цветом основного текстового блока, подстраивая его под фон.
   * @defaultValue 'dark'
   */
  variant?: 'light' | 'dark'
  /**
   * Флаг для скрытия текстовой части логотипа на мобильных устройствах.
   * Если `true`, текст скрывается на экранах меньше 'xs'.
   * @defaultValue false
   */
  hideTextOnMobile?: boolean
}

/**
 * Компонент фирменного логотипа «Хекслет Карьера».
 *
 * Отображает графическую иконку-терминал (`>_`) в синем блоке и текстовое
 * название сервиса. Поддерживает переключение цветовой схемы для корректного
 * отображения как на светлых, так и на тёмных фоновых подложках. Позволяет
 * скрывать текстовую часть на мобильных экранах для экономии места.
 *
 * @param props - Конфигурационные свойства компонента `LogoProps`.
 * @returns {JSX.Element} Группа элементов, формирующих визуальный логотип.
 */
export const Logo = ({
  variant = 'dark',
  hideTextOnMobile = false,
}: LogoProps) => {
  const logoTextColor = variant === 'dark' ? 'black' : 'white'

  return (
    <Group gap="xs" align="center">
      <ThemeIcon
        variant="filled"
        color="rgb(17, 110, 245)"
        size="lg"
        radius="md"
      >
        <Text ff="'JetBrains Mono', monospace" fw={700} c="white" fz={13}>
          &gt;_
        </Text>
      </ThemeIcon>
      <Stack gap={0} visibleFrom={hideTextOnMobile ? 'xs' : 'base'}>
        <Text fw={700} lh="xs" fz={14.5} c={logoTextColor}>
          Хекслет
        </Text>
        <Text
          fw={700}
          lh={1.2}
          style={{ letterSpacing: rem(1.2) }}
          c="rgb(124, 169, 248)"
          fz={9}
          tt="uppercase"
        >
          Карьера
        </Text>
      </Stack>
    </Group>
  )
}
