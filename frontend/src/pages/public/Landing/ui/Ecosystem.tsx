import {
  Box,
  Container,
  Grid,
  Group,
  Paper,
  Text,
  Title,
} from '@mantine/core'
import { palette } from '@ui/theme/palette'
import { NumberedList } from './NumberedList'
import { ecosystemPoints } from '../model/content'

/** Строка отчёта после тренировки: критерий, вердикт и пояснение. */
function ScoreRow({
  title,
  verdict,
  verdictColor,
  text,
  borderColor = palette.border,
  borderWidth = 1,
}: {
  title: string
  verdict: string
  verdictColor: string
  text: string
  borderColor?: string
  borderWidth?: number
}) {
  return (
    <Paper
      bg="white"
      radius={12}
      px={18}
      py={16}
      style={{ border: `${borderWidth}px solid ${borderColor}` }}
    >
      <Group justify="space-between" mb={8} wrap="nowrap">
        <Text fz={13} fw={700}>
          {title}
        </Text>
        <Text fz={12} fw={800} c={verdictColor}>
          {verdict}
        </Text>
      </Group>
      <Text fz={12} lh={1.5} c={palette.muted}>
        {text}
      </Text>
    </Paper>
  )
}

/**
 * «Встроено в экосистему Хекслета»: чем карьерный сервис отличается от
 * отдельного тула — и как выглядит честная оценка после тренировки.
 */
export function Ecosystem() {
  return (
    <Container size="lg" px={24} py={64}>
      <Grid gutter={48} align="center">
        <Grid.Col span={{ base: 12, md: 6 }}>
          <Title order={2} fz={30} mb={14} style={{ letterSpacing: '-0.7px' }}>
            Встроено в экосистему Хекслета
          </Title>
          <Text fz={14.5} lh={1.65} c={palette.text} maw={440} mb={22}>
            Карьера — не отдельный тул, а продолжение учёбы. Навыки в резюме
            подтверждаются реальными упражнениями, а не словами.
          </Text>
          <NumberedList points={ecosystemPoints} />
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 6 }}>
          <Box
            bg={palette.surface}
            px={28}
            py={26}
            style={{ border: `1px solid ${palette.border}`, borderRadius: 16 }}
          >
            <Text
              fz={12}
              fw={800}
              tt="uppercase"
              c={palette.muted}
              mb={16}
              style={{ letterSpacing: '0.8px' }}
            >
              Честная оценка после тренировки
            </Text>
            <Box mb={10}>
              <ScoreRow
                title="Подача ответа"
                verdict="уверенно ✓"
                verdictColor={palette.green}
                text="Структурировано, темп 132 слова в минуту"
              />
            </Box>
            <ScoreRow
              title="Корректность решения"
              verdict="тесты: 4 из 6"
              verdictColor={palette.orange}
              borderColor={palette.orange}
              borderWidth={1.5}
              text="Другой тренажёр похвалил бы. Наш запустил тесты: O(n²) не пройдёт нагрузку — вот разбор с Set за O(n)"
            />
          </Box>
        </Grid.Col>
      </Grid>
    </Container>
  )
}
