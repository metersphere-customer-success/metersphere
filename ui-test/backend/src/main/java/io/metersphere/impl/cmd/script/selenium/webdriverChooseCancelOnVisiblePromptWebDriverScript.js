var prompt = driver.switchTo().alert();
alertResultText = prompt.getText();
prompt.dismiss();