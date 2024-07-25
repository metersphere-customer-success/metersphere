var doTime_#{index} = Date.now();
do {
    if ((Date.now() - doTime_#{index} > "#{timeout}")) {break;}
} while(1)
