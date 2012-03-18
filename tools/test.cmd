@echo off
python tools/playgame.py "java -jar ANT/src/MyBot.jar test" "java -jar ANT/src/MyBot.jar test" "java -jar ANT/src/MyBot.jar test" --map_file tools/maps/random_walk/random_walk_03p_02.map --log_dir game_logs --turns 500 --player_seed 7 --verbose -e >>test.txt
"python tools\sample_bots\python\HoldBot.py"
