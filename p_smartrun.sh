# Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#for i; do 
#    echo $i 
     
#done

#echo $#

#echo ${7}

#echo "---------------- end -------------------------- "

# replica means that replica (or server) was started and --debug will be at position 7
replica_debug=${7}
# client means that client was started and --debug will be at position 10
client_debug=${10} 

# remove this file because if you change hosts.config or system.config you should remove this one.
#rm -rf currentView

if [[ "$replica_debug" == "--debug" || "$client_debug" == "--debug" ]]; then
    echo "Debug enabled"
    java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9000" -Dlogback.configurationFile="./config/logback-console-file.xml" -cp dist/recovery4smr-20200517.jar:lib/* $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11
else
    java -Dlogback.configurationFile="./config/logback-console-file.xml" -cp dist/recovery4smr-20200517.jar:lib/* $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11
fi


#java -cp dist/BFT-SMaRt-Parallel.jar:lib/bft-smart.jar:lib/slf4j-api-1.5.8.jar:lib/slf4j-jdk14-1.5.8.jar:lib/netty-all-4.0.36.Final.jar:lib/commons-codec-1.5.jar $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11
