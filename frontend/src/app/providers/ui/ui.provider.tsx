import { MantineProvider, createTheme } from '@mantine/core'
import '@mantine/core/styles.css'
import { palette } from './theme/palette'

/**
 * Высота шапки из прототипа. Её же читает {@link useScrollToHash},
 * чтобы якорь секции не уезжал под шапку.
 */
const HEADER_HEIGHT = 62

// Чистая светлая тема «Хекслет Карьера» (Mantine во главе). Тёмный — только сайдбар.
const theme = createTheme({
  primaryColor: 'brand',
  primaryShade: 6,
  defaultRadius: 'md',
  // Manrope и JetBrains Mono подключены в index.html — ими же набран прототип.
  fontFamily: 'Manrope, -apple-system, Segoe UI, Roboto, sans-serif',
  fontFamilyMonospace: "'JetBrains Mono', ui-monospace, monospace",
  headings: {
    fontFamily: 'Manrope, -apple-system, Segoe UI, Roboto, sans-serif',
    fontWeight: '800',
  },
  colors: {
    // Оттенки 0-5 и 7-9 подобраны вокруг фирменного #116EF5: он идёт
    // шестым, чтобы `color="brand"` без указания оттенка давал именно его.
    brand: [
      '#EAF2FE',
      '#D3E3FD',
      '#A6C6FB',
      '#79A9F9',
      '#4C8CF7',
      '#2A7AF6',
      palette.brand,
      palette.brandDark,
      '#094BAD',
      '#073A85',
    ],
  },
  other: {
    headerHeight: HEADER_HEIGHT,
  },
})

export const UIProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  return (
    <MantineProvider
      theme={theme}
      defaultColorScheme="light"
      forceColorScheme="light"
    >
      {children}
    </MantineProvider>
  )
}
