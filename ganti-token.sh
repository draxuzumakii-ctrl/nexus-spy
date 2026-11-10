#!/data/data/com.termux/files/usr/bin/bash
# Auto-replace token di 3 file. Jalankan dari ~/nexus-spy
# Usage: bash ganti-token.sh
set -e
cd ~/nexus-spy 2>/dev/null || { echo "[!] folder ~/nexus-spy tidak ada"; exit 1; }

read -rsp "Paste token GitHub baru (ghp_...): " NEWTOK
echo
if [[ -z "$NEWTOK" ]]; then echo "[!] token kosong"; exit 1; fi

FILES=(
  "apk/app/src/main/java/com/nexus/spy/GithubApi.java"
  "docs/index.html"
  "docs/dev.html"
)

for f in "${FILES[@]}"; do
  if [[ ! -f "$f" ]]; then echo "[!] tidak ada: $f"; continue; fi
  # ganti placeholder ATAU token ghp_ lama (apa pun isinya)
  sed -i -E "s/ghp_[A-Za-z0-9_]+/${NEWTOK}/g" "$f"
  echo "[✓] $f"
done

echo ""
echo "Verifikasi sisa placeholder:"
grep -rn "ghp_ISI_TOKEN_LO" . --exclude-dir=.git || echo "  (bersih, tidak ada placeholder)"
echo ""
echo "Token sekarang terpasang di:"
grep -rn "${NEWTOK}" apk/app/src/main/java/com/nexus/spy/GithubApi.java docs/ 2>/dev/null | sed 's/'"${NEWTOK}"'/ghp_***REDACTED***/g'grep -rn "ghp_ISI_TOKEN_LO" . --exclude-dir=.git --exclude=ganti-token.sh || echo "  (bersih, tidak ada placeholder)"
