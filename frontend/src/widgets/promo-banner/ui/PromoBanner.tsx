import { Box, Text } from '@mantine/core'
import { Link } from 'react-router'

/**
 * Промо-полоса над шапкой публичного сайта.
 *
 * Стоит именно над `Header`, а не внутри страницы: в прототипе она уезжает
 * вверх вместе со скроллом, тогда как шапка остаётся приклеенной. Если
 * положить полосу внутрь `Header`, приклеенной окажется и она.
 */
export function PromoBanner() {
  return (
    <Box
      component={Link}
      to="/register"
      bg="brand"
      py={9}
      px={20}
      ta="center"
      display="block"
      style={{ textDecoration: 'none' }}
    >
      <Text c="white" fw={600} fz={12.5}>
        Выпускникам Хекслета 2026 года тариф «Про» — бесплатно на 6 месяцев →
      </Text>
    </Box>
  )
}
