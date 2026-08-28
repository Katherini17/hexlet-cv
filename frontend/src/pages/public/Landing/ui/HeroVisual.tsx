import { Box, Group, Paper, Stack, Text } from '@mantine/core'
import { IconCheck } from '@tabler/icons-react'
import { palette, shadows } from '@ui/theme/palette'

/** Колонка канбана в макете трекера: заголовок и карточки отклика. */
function KanbanColumn({
  label,
  labelColor,
  bg,
  children,
}: {
  label: string
  labelColor: string
  bg: string
  children: React.ReactNode
}) {
  return (
    <Box flex={1} bg={bg} p={8} style={{ borderRadius: 9 }}>
      <Text fz={10.5} fw={800} c={labelColor} px={4} pt={2} pb={7}>
        {label}
      </Text>
      <Stack gap={7}>{children}</Stack>
    </Box>
  )
}

/** Карточка внутри колонки: компания и строка со статусом. */
function KanbanCard({
  company,
  note,
  noteColor = palette.faint,
  borderColor = palette.border,
  borderWidth = 1,
  noteBold = false,
}: {
  company: string
  note: string
  noteColor?: string
  borderColor?: string
  borderWidth?: number
  noteBold?: boolean
}) {
  return (
    <Box
      bg="white"
      px={9}
      py={8}
      style={{ border: `${borderWidth}px solid ${borderColor}`, borderRadius: 7 }}
    >
      <Text fz={11} fw={700}>
        {company}
      </Text>
      <Text fz={9.5} c={noteColor} fw={noteBold ? 700 : 400} mt={2}>
        {note}
      </Text>
    </Box>
  )
}

/**
 * Иллюстрация героя: макет трекера откликов и три всплывающие карточки
 * (матч с вакансией, прогон тестов в AI-собесе, проверка резюме на ATS).
 *
 * Блоки позиционированы абсолютно — как в прототипе, где они намеренно
 * перекрывают друг друга. Поэтому у контейнера фиксированная высота: без неё
 * секция схлопнется, ведь абсолютные дети её не растягивают.
 */
export function HeroVisual() {
  return (
    <Box pos="relative" h={420}>
      <Paper
        pos="absolute"
        left={0}
        top={26}
        w="78%"
        bg="white"
        radius={14}
        style={{
          border: `1px solid ${palette.border}`,
          boxShadow: shadows.raised,
          overflow: 'hidden',
        }}
      >
        <Group
          gap={6}
          px={14}
          py={11}
          style={{ borderBottom: `1px solid #F0F1F4` }}
        >
          {[0, 1, 2].map((dot) => (
            <Box
              key={dot}
              w={9}
              h={9}
              bg={palette.border}
              style={{ borderRadius: '50%' }}
            />
          ))}
          <Text ml={8} fz={11} fw={700} c={palette.faint}>
            Отклики · доска
          </Text>
        </Group>
        <Group gap={10} p={14} align="stretch">
          <KanbanColumn
            label="ОТКЛИК · 4"
            labelColor={palette.muted}
            bg={palette.surface}
          >
            <KanbanCard company="Ozon Tech" note="резюме v3 · 2 дня" />
            <KanbanCard company="Авито" note="+ реферал · сегодня" />
          </KanbanColumn>
          <KanbanColumn
            label="СОБЕС · 2"
            labelColor={palette.muted}
            bg={palette.surface}
          >
            <KanbanCard
              company="Т-Банк"
              note="завтра 11:00"
              noteColor={palette.brandDark}
              borderColor={palette.brand}
              borderWidth={1.5}
              noteBold
            />
          </KanbanColumn>
          <KanbanColumn
            label="ОФФЕР · 1"
            labelColor={palette.greenDark}
            bg={palette.greenSoft}
          >
            <KanbanCard
              company="Эвотор"
              note="155 000 ₽"
              noteColor={palette.greenDark}
              borderColor={palette.green}
              borderWidth={1.5}
              noteBold
            />
          </KanbanColumn>
        </Group>
      </Paper>

      <Paper
        pos="absolute"
        right={0}
        top={0}
        bg="white"
        radius={12}
        px={16}
        py={13}
        style={{
          border: `1px solid ${palette.border}`,
          boxShadow: shadows.card,
        }}
      >
        <Group gap={11} wrap="nowrap">
          <svg width="44" height="44" viewBox="0 0 44 44" aria-hidden="true">
            <circle
              cx="22"
              cy="22"
              r="18"
              fill="none"
              stroke="#EDEFF3"
              strokeWidth="4.5"
            />
            <circle
              cx="22"
              cy="22"
              r="18"
              fill="none"
              stroke={palette.green}
              strokeWidth="4.5"
              strokeLinecap="round"
              strokeDasharray="104 113"
              transform="rotate(-90 22 22)"
            />
            <text
              x="22"
              y="26"
              textAnchor="middle"
              fontSize="12.5"
              fontWeight="700"
              fill={palette.ink}
              fontFamily="Manrope"
            >
              92
            </text>
          </svg>
          <Box>
            <Text fz={12} fw={800}>
              Матч с вакансией
            </Text>
            <Text fz={10.5} c={palette.muted} mt={1}>
              Авито · Junior Frontend
            </Text>
          </Box>
        </Group>
      </Paper>

      <Paper
        pos="absolute"
        right={22}
        bottom={44}
        w={250}
        bg={palette.ink}
        radius={12}
        px={16}
        py={14}
        style={{ boxShadow: shadows.dark }}
      >
        <Group gap={7} mb={9}>
          <Text
            fz={9.5}
            fw={800}
            c={palette.aiText}
            px={6}
            py={2}
            bg={palette.aiSoft}
            style={{ borderRadius: 5 }}
          >
            AI-СОБЕС
          </Text>
          <Text ff="monospace" fz={10.5} c="#8B909E">
            solution.js
          </Text>
        </Group>
        <Text ff="monospace" fz={10.5} lh={1.6} c="#E8EAF0">
          <Text span c="#C678DD" inherit>
            const
          </Text>{' '}
          seen ={' '}
          <Text span c="#C678DD" inherit>
            new
          </Text>{' '}
          <Text span c="#61AFEF" inherit>
            Set
          </Text>
          ();
        </Text>
        <Group gap={7} mt={9}>
          <IconCheck size={13} stroke={2.6} color="#37B24D" />
          <Text fz={11} fw={700} c="#69DB7C">
            Тесты: 6 из 6 · O(n)
          </Text>
        </Group>
      </Paper>

      <Paper
        pos="absolute"
        left={34}
        bottom={0}
        bg="white"
        radius={12}
        px={15}
        py={12}
        style={{
          border: `1px solid ${palette.border}`,
          boxShadow: shadows.card,
        }}
      >
        <Group gap={9} wrap="nowrap">
          <IconCheck size={15} stroke={2.4} color={palette.green} />
          <Text fz={12} fw={700}>
            Резюме прошло ATS-проверку
          </Text>
        </Group>
      </Paper>
    </Box>
  )
}
