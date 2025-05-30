#!/bin/bash

# Fix AnalisiPrestazioniDao.kt
cd /workspace/Mover1

# 1. Fix field names for entities that have them
sed -i 's/fitness/ctl/g; s/fatigue/atl/g; s/form/forma/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/zonaCardiaca1/zona1Tempo/g; s/zonaCardiaca2/zona2Tempo/g; s/zonaCardiaca3/zona3Tempo/g; s/zonaCardiaca4/zona4Tempo/g; s/zonaCardiaca5/zona5Tempo/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/zonaPotenza1/zona1Tempo/g; s/zonaPotenza2/zona2Tempo/g; s/zonaPotenza3/zona3Tempo/g; s/zonaPotenza4/zona4Tempo/g; s/zonaPotenza5/zona5Tempo/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/performanceScore/indiceEfficienza/g; s/efficiencyScore/efficiencyFactor/g; s/vo2Stimato/trainingStressScore/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt

# 2. Fix user field references for FitnessFreshness only
sed -i 's/WHERE atleta = :atleta/WHERE utenteId = :utenteId/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/AND atleta = :atleta/AND utenteId = :utenteId/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/a1\.atleta = :atleta/a1.utenteId = :utenteId/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i 's/a2\.atleta = :atleta/a2.utenteId = :utenteId/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt

# 3. Fix function parameters
sed -i 's/atleta: String/utenteId: String/g' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt

# 4. Remove user filters from entities that don't have utenteId field
# AnalisiZone, MetrichePerformance, AnalisiPassoGara don't have utenteId
sed -i '/FROM analisi_zone/,/ORDER BY\|GROUP BY/ { s/WHERE utenteId = :utenteId//g; s/AND utenteId = :utenteId//g; }' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i '/FROM metriche_performance/,/ORDER BY\|GROUP BY/ { s/WHERE utenteId = :utenteId//g; s/AND utenteId = :utenteId//g; }' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt
sed -i '/FROM analisi_passo_gara/,/ORDER BY\|GROUP BY/ { s/WHERE utenteId = :utenteId//g; s/AND utenteId = :utenteId//g; }' app/src/main/java/com/example/mover/data/AnalisiPrestazioniDao.kt

# 5. Fix ObiettiviDao
sed -i 's/dataCreazione/dataOra/g' app/src/main/java/com/example/mover/database/dao/ObiettiviDao.kt

echo "All fixes applied!"