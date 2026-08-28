import { Box, Text, Title } from '@mantine/core'
import { palette } from '@ui/theme/palette'

/**
 * Заголовок секции лендинга: h2 и абзац-подводка под ним.
 *
 * Ширина ограничена 520 пикселями — в прототипе подводка везде переносится
 * раньше, чем кончается контейнер, иначе строка становится слишком длинной
 * для чтения.
 */
export function SectionHeading({
  title,
  text,
  mb = 36,
}: {
  title: string
  text: string
  mb?: number
}) {
  return (
    <Box maw={520} mb={mb}>
      <Title order={2} fz={30} mb={10} style={{ letterSpacing: '-0.7px' }}>
        {title}
      </Title>
      <Text fz={14.5} lh={1.6} c={palette.text}>
        {text}
      </Text>
    </Box>
  )
}
