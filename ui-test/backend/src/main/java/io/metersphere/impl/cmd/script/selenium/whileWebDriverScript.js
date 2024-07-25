var whileTime_#{index} = Date.now();
while(parseProgramScript("${target}") && (Date.now() - whileTime_#{index} < "#{timeout}")) {}
