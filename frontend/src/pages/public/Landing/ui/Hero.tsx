import { Box, Button, Container, Grid, Group, Text, Title } from '@mantine/core'
import { Link } from 'react-router'
import { palette } from '@ui/theme/palette'
import { HeroVisual } from './HeroVisual'

/**
 * Первый экран лендинга: обещание сервиса, две кнопки и макет продукта справа.
 *
 * Иллюстрация скрыта до `md`: она собрана из абсолютно позиционированных
 * карточек фиксированной высоты и на узком экране наезжает сама на себя.
 */
export function Hero() {
  return (
    <Container size="lg" px={24} pt={72} pb={64}>
      <Grid gutter={48} align="center" columns={21}>
        <Grid.Col span={{ base: 21, md: 11 }}>
          <Group
            gap={8}
            display="inline-flex"
            px={13}
            py={5}
            mb={20}
            bg={palette.brandSoft}
            style={{ borderRadius: 99 }}
          >
            <Box
              w={7}
              h={7}
              bg={palette.brand}
              style={{ borderRadius: '50%' }}
            />
            <Text fz={12} fw={700} c={palette.brandDark}>
              Сервис Хекслета для поиска работы в IT
            </Text>
          </Group>

          <Title
            order={1}
            fz={{ base: 34, sm: 46 }}
            lh={1.12}
            mb={18}
            style={{ letterSpacing: '-1.2px' }}
          >
            От отклика
            <br />
            до оффера —
            <br />
            в одном сервисе
          </Title>

          <Text fz={16.5} lh={1.6} c={palette.text} maw={440} mb={28}>
            Вакансии с трёх площадок, резюме с AI-ревью, письма, трекер откликов
            и тренировочные собеседования, где код проверяется реальными тестами.
          </Text>

          <Group gap={12}>
            <Button component={Link} to="/register" size="md" h={46} radius={9}>
              Начать бесплатно
            </Button>
            <Button
              component={Link}
              to="/app"
              size="md"
              h={46}
              radius={9}
              variant="default"
            >
              Смотреть демо
            </Button>
          </Group>

          <Text fz={12.5} c={palette.muted} mt={18}>
            Бесплатно для студентов Хекслета · без карты
          </Text>
        </Grid.Col>

        <Grid.Col span={{ base: 21, md: 10 }} visibleFrom="md">
          <HeroVisual />
        </Grid.Col>
      </Grid>
    </Container>
  )
}
