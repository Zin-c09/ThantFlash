#!/bin/bash
# Java ကို compile လုပ်ပြီး run မယ့် script
# သုံးနည်း:  chmod +x run.sh  →  ./run.sh
set -e                      # error တက်ရင် ချက်ချင်းရပ်

cd "$(dirname "$0")"        # script ရှိတဲ့ folder ထဲ ဝင်
mkdir -p out                # compile ထွက်လာမယ့် .class ဖိုင်တွေ ထားမယ့် folder

echo ">> Compiling..."
javac -d out src/*.java

echo ">> Running..."
java -cp out Main
