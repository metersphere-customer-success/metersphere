# ui-test


本地IDEA执行ui场景需要配置VM options

```
-Dpolyglot.js.nashorn-compat=true -Dpolyglot.log.file=/opt/metersphere/logs/ui-test/info.log --add-opens java.base/jdk.internal.loader=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -Dnashorn.args=--no-deprecation-warning
```