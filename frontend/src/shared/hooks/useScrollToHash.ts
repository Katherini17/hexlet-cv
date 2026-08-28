import { useEffect } from 'react'
import { useLocation } from 'react-router'
import { useMantineTheme } from '@mantine/core'

const DEFAULT_HEADER_HEIGHT = 64

/**
 * Хук для реализации плавного скролла к элементам страницы при наличии хэша в URL.
 * Учитывает динамическую высоту фиксированной шапки приложения или использует значение по умолчанию {@link DEFAULT_HEADER_HEIGHT}.
 */
export function useScrollToHash() {
  const { hash, pathname } = useLocation()
  const { other } = useMantineTheme()
  const headerHeight = other?.headerHeight ?? DEFAULT_HEADER_HEIGHT

  useEffect(() => {
    if (hash) {
      const element = document.getElementById(hash.replace('#', ''))

      if (element) {
        const absoluteElementTop =
          element.getBoundingClientRect().top + window.scrollY

        window.scrollTo({
          top: absoluteElementTop - headerHeight,
          behavior: 'smooth',
        })
      }
    } else if (pathname) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, [hash, pathname, headerHeight])
}
