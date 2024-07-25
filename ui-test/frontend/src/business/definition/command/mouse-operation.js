import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition";
import CommandVODefinition from "@/business/definition/command/vo/command-vo";
import { cloneDeep } from "lodash-es"

const mouseOperation = [
  {
    "name": "cmdMouseClick",
    "command": "cmdMouseClick",
    "type": "MsUiCommand",
    "viewType": "mouseOperation",
    "cnDesc": "模拟鼠标点击的操作，支持单击/双击/按下/弹起",
    "twDesc": "模擬鼠標點擊的操作，支持單擊/雙擊/按下/彈起",
    "enDesc": "Simulate the operation of mouse click, support single click / double click / press / pop up",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.click,
      AtomicCommandDefinition.clickAt,
      AtomicCommandDefinition.rightClick,
      AtomicCommandDefinition.rightClickAt,
      AtomicCommandDefinition.doubleClick,
      AtomicCommandDefinition.doubleClickAt,
      AtomicCommandDefinition.mouseDown,
      AtomicCommandDefinition.mouseDownAt,
      AtomicCommandDefinition.mouseUp,
      AtomicCommandDefinition.mouseUpAt,
    ],
    "component": "MouseClick",
    "vo": cloneDeep(CommandVODefinition.MouseClickVO)
  },
  {
    "name": "cmdMouseMove",
    "command": "cmdMouseMove",
    "type": "MsUiCommand",
    "viewType": "mouseOperation",
    "cnDesc": "将鼠标悬停在网页元素的上方",
    "twDesc": "將鼠標懸停在網頁元素的上方",
    "enDesc": "Hover over a webpage element",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.mouseOut,
      AtomicCommandDefinition.mouseOver,
      AtomicCommandDefinition.mouseMoveAt,
    ],
    "component": "MouseMove",
    "vo": cloneDeep(CommandVODefinition.MouseMoveVO)
  },
  {
    "name": "cmdMouseDrag",
    "command": "cmdMouseDrag",
    "type": "MsUiCommand",
    "viewType": "mouseOperation",
    "cnDesc": "模拟鼠标将元素从某个位置拖到另一个位置",
    "twDesc": "模擬鼠標將元素從某個位置拖到另一個位置",
    "enDesc": "Simulate mouse dragging an element from one location to another",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      //拖拽一个元素到另一个元素上
      AtomicCommandDefinition.dragAndDropToObject,
      //拖拽一个元素到另一个元素上
      AtomicCommandDefinition.dragAndDropToObjectAt,
      //在元素内部根据给定坐标拖拽 画图
      AtomicCommandDefinition.mouseDragAndDropAt,
    ],
    "component": "MouseDrag",
    "vo": cloneDeep(CommandVODefinition.MouseDragVO)
  }
];
export default mouseOperation;
