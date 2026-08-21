import { Box, Button, Container, Group, Text, Title } from '@mantine/core'
import { Link } from 'react-router'
import { palette } from '@ui/theme/palette'

/**
 * Финальный призыв: тёмный блок перед футером.
 *
 * Вторая кнопка — не `variant="default"`, а прозрачная с белой рамкой:
 * на тёмной подложке светлая кнопка перетягивала бы внимание с основной.
 */
export function Cta() {
  return (
    <Container size="lg" px={24} pb={72}>
      <Box bg={palette.ink} px={56} py={52} style={{ borderRadius: 20 }}>
        <Group gap={40} align="center" wrap="wrap">
          <Box flex={1} miw={320}>
            <Title
              order={2}
              fz={28}
              c="white"
              mb={10}
              style={{ letterSpacing: '-0.6px' }}
            >
              Начните с резюме — это 5 минут
            </Title>
            <Text fz={14} lh={1.6} c={palette.onInk}>
              Импортируйте профиль с hh.ru или соберите с нуля — AI-ревью сразу
              покажет, что чинить.
            </Text>
          </Box>
          <Group gap={12}>
            <Button component={Link} to="/register" h={46} radius={9} size="md">
              Начать бесплатно
            </Button>
            <Button
              component={Link}
              to="/app"
              h={46}
              radius={9}
              size="md"
              variant="outline"
              c="white"
              style={{ borderColor: 'rgba(255, 255, 255, 0.25)' }}
            >
              Смотреть демо
            </Button>
          </Group>
        </Group>
      </Box>
    </Container>
  )
}
