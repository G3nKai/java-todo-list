const { test, expect } = require('@playwright/test');

test('Создание задачи', async ({ page }) => {
  await page.goto('/');
  await page.click('button.btn-success'); // Нажимаем "Добавить задачу"
  await page.fill('input[placeholder="Название задачи"]', 'Тестовая задача');
  await page.click('button.btn-primary'); // Нажимаем "Создать"
  await expect(page.locator('.task-item')).toContainText('Тестовая задача');
});
