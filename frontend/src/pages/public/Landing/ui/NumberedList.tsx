import { Box, Group, Stack, Text } from '@mantine/core'
import { palette } from '@ui/theme/palette'
import type { NumberedPoint } from '../model/content'

/**
 * Нумерованный список с квадратными плашками номеров.
 *
 * Встречается в двух секциях с разными акцентами: экосистема — синяя,
 * коммерческие проекты — зелёная.
 */
export function NumberedList({
  points,
  accent = 'brand',
  gap = 14,
}: {
  points: NumberedPoint[]
  accent?: 'brand' | 'green'
  gap?: number
}) {
  const bg = accent === 'brand' ? palette.brandSoft : palette.greenSoft
  const color = accent === 'brand' ? palette.brandDark : palette.greenDark

  return (
    <Stack gap={gap}>
      {points.map((point, index) => (
        <Group key={point.lead} gap={12} align="flex-start" wrap="nowrap">
          <Box
            w={26}
            h={26}
            bg={bg}
            c={color}
            fz={12.5}
            fw={800}
            ta="center"
            style={{ borderRadius: 8, flex: 'none', lineHeight: '26px' }}
          >
            {index + 1}
          </Box>
          <Text fz={13.5} lh={1.55} c={palette.inkSoft}>
            <Text span fw={700} inherit>
              {point.lead}
            </Text>{' '}
            {point.text}
          </Text>
        </Group>
      ))}
    </Stack>
  )
}
