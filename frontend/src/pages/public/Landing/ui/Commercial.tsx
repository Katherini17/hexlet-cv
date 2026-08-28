import {
  Box,
  Button,
  Container,
  Grid,
  Group,
  Paper,
  Stack,
  Text,
  Title,
} from '@mantine/core'
import { Link } from 'react-router'
import { palette, shadows } from '@ui/theme/palette'
import { NumberedList } from './NumberedList'
import { commercialPoints } from '../model/content'

const stack = ['React', 'TypeScript', 'REST']

/**
 * Коммерческие проекты Хекслета: команда студентов делает заказной продукт,
 * и это превращается в строку опыта в резюме.
 *
 * Секция без своей подложки — идёт сразу за экосистемой на белом фоне,
 * поэтому верхнего отступа у контейнера нет.
 */
export function Commercial() {
  return (
    <Container size="lg" px={24} pb={64}>
      <Grid gutter={48} align="center">
        <Grid.Col span={{ base: 12, md: 6 }}>
          <Group
            gap={8}
            display="inline-flex"
            px={13}
            py={5}
            mb={16}
            bg={palette.greenSoft}
            style={{ borderRadius: 99 }}
          >
            <Box
              w={7}
              h={7}
              bg={palette.green}
              style={{ borderRadius: '50%' }}
            />
            <Text fz={12} fw={700} c={palette.greenDark}>
              Опыт работы — до оффера
            </Text>
          </Group>

          <Title order={2} fz={30} mb={14} style={{ letterSpacing: '-0.7px' }}>
            Коммерческие проекты Хекслета
          </Title>
          <Text fz={14.5} lh={1.65} c={palette.text} maw={440} mb={20}>
            Команды по шесть студентов делают заказные продукты для реальных
            компаний: спринты, код-ревью тимлида из индустрии, демо заказчику и
            релиз. Через три месяца в резюме — опыт, о котором есть что
            рассказать на собеседовании.
          </Text>

          <Box mb={24}>
            <NumberedList points={commercialPoints} accent="green" gap={12} />
          </Box>

          <Group gap={12}>
            <Button component={Link} to="/register" h={44} radius={9}>
              Подать заявку
            </Button>
            <Button
              component={Link}
              to="/app"
              h={44}
              radius={9}
              variant="default"
            >
              Открытые наборы
            </Button>
          </Group>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 6 }}>
          <Stack gap={12}>
            <Paper
              bg="white"
              radius={14}
              px={24}
              py={20}
              style={{
                border: `1px solid ${palette.border}`,
                boxShadow: shadows.cardSoft,
              }}
            >
              <Group gap={8} mb={8}>
                <Text
                  fz={11}
                  fw={800}
                  tt="uppercase"
                  c={palette.greenDark}
                  bg={palette.greenSoft}
                  px={9}
                  py={3}
                  style={{ borderRadius: 99, letterSpacing: '0.4px' }}
                >
                  запущен
                </Text>
                <Text fz={12} c={palette.faint}>
                  кейс выпуска 2026
                </Text>
              </Group>
              <Text fz={16} fw={800} mb={5}>
                Сервис предзаказа для сети пекарен «Мука»
              </Text>
              <Text fz={13} lh={1.6} c={palette.text} mb={10}>
                2 000 заказов в месяц · команда из 6 студентов + тимлид из Авито
                · 3 месяца
              </Text>
              <Group gap={6}>
                {stack.map((item) => (
                  <Text
                    key={item}
                    fz={11.5}
                    fw={600}
                    c="#3E434D"
                    bg="#F1F3F7"
                    px={9}
                    py={2}
                    style={{ borderRadius: 6 }}
                  >
                    {item}
                  </Text>
                ))}
              </Group>
            </Paper>

            <Paper bg={palette.ink} c="white" radius={14} px={24} py={20}>
              <Group gap={8} mb={8}>
                <Text
                  fz={11}
                  fw={800}
                  tt="uppercase"
                  c="#FFC078"
                  bg="rgba(240, 140, 0, 0.35)"
                  px={9}
                  py={3}
                  style={{ borderRadius: 99, letterSpacing: '0.4px' }}
                >
                  набор открыт
                </Text>
                <Text fz={12} c="#8B909E">
                  заявки до 15 июля
                </Text>
              </Group>
              <Text fz={16} fw={800} mb={5}>
                Сервис записи для сети клиник
              </Text>
              <Text fz={13} lh={1.6} c={palette.onInk} mb={12}>
                2 слота Frontend · старт 21 июля · тимлид — сеньор из Контура
              </Text>
              <Button component={Link} to="/register" h={36} radius={8} fz={13}>
                Подать заявку
              </Button>
            </Paper>
          </Stack>
        </Grid.Col>
      </Grid>
    </Container>
  )
}
