EXEC=./PreliminaryJudge # The judge program
MAP_PATH=./map.txt # The map file
JAR_PATH=./XXX.jar # The jar file

RESULT_PATH=./result.log
ERROR_PATH=./err.log

for H_SHIP_CAPACITY_THRESHOLD in {10,30,50,70};
do
    for H_MAX_SHIP_LOAD_TIME in {0.1,0.3,0.5,0.7};
    do
        for H_SHIP_REDIRECT_THRESHOLD in {1,2,3,4};
        do
            for H_ASTAR_MAX_TIME_GOODS in {1,5,10,15};
            do
                for H_ASTAR_MAX_TIME_DOCK in {1,5,10,15};
                do
                    for H_DOCK_PUNISH in {1,10,100};
                    do
                        for H_BOT_FIND_GOOD_ITERATOR in {true,false};
                        do
                            for H_BOT_FIND_DOCK_ITERATOR in {true,false};
                            do
                                echo "$H_SHIP_CAPACITY_THRESHOLD $H_MAX_SHIP_LOAD_TIME $H_SHIP_REDIRECT_THRESHOLD $H_ASTAR_MAX_TIME_GOODS $H_ASTAR_MAX_TIME_DOCK $H_DOCK_PUNISH $H_BOT_FIND_GOOD_ITERATOR $H_BOT_FIND_DOCK_ITERATOR" >> $RESULT_PATH;
                                $EXEC -l NONE -m $MAP_PATH "java -jar $JAR_PATH $H_SHIP_CAPACITY_THRESHOLD $H_MAX_SHIP_LOAD_TIME $H_SHIP_REDIRECT_THRESHOLD $H_ASTAR_MAX_TIME_GOODS $H_ASTAR_MAX_TIME_DOCK $H_DOCK_PUNISH $H_BOT_FIND_GOOD_ITERATOR $H_BOT_FIND_DOCK_ITERATOR" >> $RESULT_PATH 2>$ERROR_PATH;
                            done
                        done
                    done
                done
            done
        done
    done
done
