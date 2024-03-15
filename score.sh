for map in $(ls ~/Competitions/2024-HUAWEI/MacOSReleasev1.1/maps); do
  echo "Evaluating on $map ..."
  ~/Competitions/2024-HUAWEI/MacOSReleasev1.1/PreliminaryJudge -l 'ERR' -m ~/Competitions/2024-HUAWEI/MacOSReleasev1.1/maps/$map 'java -jar ~/Gitspace/2024-Huawei-JAVA/out/artifacts/2024_Huawei_JAVA_jar/2024-Huawei-JAVA.jar'
done