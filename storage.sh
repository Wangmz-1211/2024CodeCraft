for i in {0..9};
do
    grep "SHDOCK$i" info.log > dock$i.log
    grep "GHBOT$i" info.log > bot$i.log
done
for i in {0..4};
do
    grep "SHSHIP$i" info.log > ship$i.log
done
grep "HGOODS" info.log > goods.log
