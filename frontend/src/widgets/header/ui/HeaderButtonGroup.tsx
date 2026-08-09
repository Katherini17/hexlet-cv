import { Button, Flex } from '@mantine/core'
import { Link } from 'react-router'

/**
 * Компонент группы кнопок авторизации в шапке сайта.
 *
 * Отображает две адаптивные кнопки: «Войти» (ссылка на авторизацию)
 * и «Начать бесплатно» (акцентная ссылка на регистрацию). Разметка автоматически
 * подстраивает внутренние отступы и расстояние между кнопками под мобильные и десктопные экраны.
 *
 * @returns Элемент разметки с кнопками входа и регистрации.
 */
export function HeaderButtonGroup() {
  return (
    <Flex gap={{ base: 'xs', sm: 28 }}>
      <Button
        component={Link}
        to="/login"
        variant="subtle"
        c="rgb(62, 67, 77)"
        px={{ base: 10, sm: 'md' }}
        fz={13.5}
        fw={700}
      >
        Войти
      </Button>
      <Button
        component={Link}
        to="/register"
        px={{ base: 10, sm: 'md' }}
        fz={13.5}
        fw={700}
        bg="rgb(17, 110, 245)"
      >
        Начать бесплатно
      </Button>
    </Flex>
  )
}
