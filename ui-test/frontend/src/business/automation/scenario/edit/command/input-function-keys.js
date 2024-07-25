let functionKeys = [
  {
    name: "全选(Ctrl+A)",
    content: "${KEY_CONTROL+A}",
  },
  {
    name: "复制(Ctrl+C)",
    content: "${KEY_CONTROL+C}",
  },
  {
    name: "粘贴(Ctrl+V)",
    content: "${KEY_CONTROL+V}",
  },
  {
    name: "剪切(Ctrl+X)",
    content: "${KEY_CONTROL+X}",
  },
  {
    name: "回车键(Enter)",
    content: "${KEY_ENTER}",
  },
  {
    name: "删除键(BackSpace)",
    content: "${KEY_BACK_SPACE}",
  },
  {
    name: "空格键(Space)",
    content: "${KEY_SPACE}",
  },
  {
    name: "制表键(Tab)",
    content: "${KEY_TAB}",
  },
  {
    name: "↑ (Up)",
    content: "${KEY_UP}",
  },

  {
    name: "↓ (Down)",
    content: "${KEY_DOWN}",
  },
  {
    name: "← (Left)",
    content: "${KEY_LEFT}",
  },
  {
    name: "→ (Right)",
    content: "${KEY_RIGHT}",
  },
  {
    name: "Esc键(Esc)",
    content: "${KEY_ESCAPE}",
  },
  {
    name: "Shift键(Shift)",
    content: "${KEY_SHIFT}",
  },
  {
    name: "Ctrl键(Ctrl)",
    content: "${KEY_CONTROL}",
  },
  {
    name: "Alt键(Alt)",
    content: "${KEY_ALT}",
  },
  {
    name: "PageUp键(PageUp)",
    content: "${KEY_PAGE_UP}",
  },
  {
    name: "PageDown键(PageDown)",
    content: "${KEY_PAGE_DOWN}",
  },
  {
    name: "键盘F1(F1)",
    content: "${KEY_F1}",
  },
  {
    name: "键盘F2(F2)",
    content: "${KEY_F2}",
  },
  {
    name: "键盘F3(F3)",
    content: "${KEY_F3}",
  },
  {
    name: "键盘F4(F4)",
    content: "${KEY_F4}",
  },
  {
    name: "键盘F5(F5)",
    content: "${KEY_F5}",
  },
  {
    name: "键盘F6(F6)",
    content: "${KEY_F6}",
  },
  {
    name: "键盘F7(F7)",
    content: "${KEY_F7}",
  },
  {
    name: "键盘F8(F8)",
    content: "${KEY_F8}",
  },
  {
    name: "键盘F9(F9)",
    content: "${KEY_F9}",
  },
  {
    name: "键盘F10(F10)",
    content: "${KEY_F10}",
  },
  {
    name: "键盘F11(F11)",
    content: "${KEY_F11}",
  },
  {
    name: "键盘F12(F12)",
    content: "${KEY_F12}",
  },
  {
    name: "Meta(Meta)",
    content: "${KEY_META}",
  },
  {
    name: "Command(Command)",
    content: "${KEY_COMMAND}",
  },
  {
    name: "等号(Equals)",
    content: "${KEY_EQUALS}",
  },
  {
    name: "分号(Semicolon)",
    content: "${KEY_SEMICOLON}",
  },
  {
    name: "加(Add)",
    content: "${KEY_ADD}",
  },
  {
    name: "减(Subtract)",
    content: "${KEY_SUBTRACT}",
  },
  {
    name: "乘(Multiply)",
    content: "${KEY_MULTIPLY}",
  },
  {
    name: "除(Divide)",
    content: "${KEY_DIVIDE}",
  },

  //暂时不显示 数字键
  // {
  //   name: "数字0(num0)",
  //   content: "${KEY_NUMPAD0}",
  // },
  // {
  //   name: "数字1(num1)",
  //   content: "${KEY_NUMPAD1}",
  // },
  // {
  //   name: "数字2(num2)",
  //   content: "${KEY_NUMPAD2}",
  // },
  // {
  //   name: "数字3(num3)",
  //   content: "${KEY_NUMPAD3}",
  // },
  // {
  //   name: "数字4(num4)",
  //   content: "${KEY_NUMPAD4}",
  // },
  // {
  //   name: "数字5(num5)",
  //   content: "${KEY_NUMPAD5}",
  // },
  // {
  //   name: "数字6(num6)",
  //   content: "${KEY_NUMPAD6}",
  // },
  // {
  //   name: "数字7(num7)",
  //   content: "${KEY_NUMPAD7}",
  // },
  // {
  //   name: "数字8(num8)",
  //   content: "${KEY_NUMPAD8}",
  // },
  // {
  //   name: "数字9(num9)",
  //   content: "${KEY_NUMPAD9}",
  // },
];

export default function getAllFunctionKeys() {
  functionKeys.forEach((v, i) => {
    v.id = i + 1;
  });
  return functionKeys;
}
