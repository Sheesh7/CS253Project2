import os
import sys
import random
import string
import math
import re


def load_names(path):
    out = []
    seen = set()
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            s = line.strip()
            if s and s not in seen:
                seen.add(s)
                out.append(s)
    return out


def load_eff_words(path):
    words = []
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            s = line.strip()
            if not s:
                continue
            parts = s.split()
            w = parts[-1] if parts else ""
            w = re.sub(r"[^a-z]", "", w.lower())
            if len(w) >= 3:
                words.append(w)
    words = list(dict.fromkeys(words))
    if len(words) < 1000:
        raise SystemExit("wordlist too small after filtering")
    return words


def rand_alnum8(rng):
    return "".join(rng.choices(string.ascii_uppercase + string.digits, k=8))


def binom_speed(rng):
    return 1 + sum(1 if rng.random() < 0.25 else 0 for _ in range(4))


def pick_unique_full_names(U, firsts, lasts, rng):
    total = len(firsts) * len(lasts)
    if U > total:
        U = total
    chosen = set()
    out = []
    while len(out) < U:
        f = rng.choice(firsts)
        l = rng.choice(lasts)
        key = (f, l)
        if key in chosen:
            continue
        chosen.add(key)
        out.append(key)
    rng.shuffle(out)
    return out


def alpha_only(s):
    return re.sub(r"[^A-Za-z]", "", s)


def alpha_suffix(n):
    if n <= 0:
        return ""
    s = []
    while n > 0:
        n -= 1
        s.append(chr(ord("a") + (n % 26)))
        n //= 26
    return "".join(reversed(s))


def email_from_name_alpha(first, last, seen_local):
    f = alpha_only(first)
    l = alpha_only(last)
    if not f:
        f = "x"
    if not l:
        l = "x"
    base = (f[0] + l[:7]).lower()
    if base not in seen_local:
        seen_local[base] = 0
        local = base
    else:
        seen_local[base] += 1
        local = base + alpha_suffix(seen_local[base])
    return local + "@purdue.edu"


def seconds_to_hhmmss(s):
    h = s // 3600
    m = (s % 3600) // 60
    x = s % 60
    return f"{h:02d}:{m:02d}:{x:02d}"


def gen_submission_times(N, base_ts, p0, max_step, rng):
    t = base_ts
    out = []
    for i in range(N):
        if i == 0:
            out.append(t)
        else:
            inc = 0 if rng.random() < p0 else rng.randint(1, max_step)
            t += inc
            out.append(t)
    return out


def lcg_params(mod, rng):
    if mod <= 1:
        return 1, 0
    while True:
        a = rng.randrange(2, mod - 1)
        if math.gcd(a, mod) == 1:
            break
    b = rng.randrange(0, mod)
    return a, b


def pair_name_at(i, W, a, b):
    L = len(W)
    mod = L * L
    j = (a * i + b) % mod
    x = j % L
    y = (j // L) % L
    return W[x] + "-" + W[y]


def triple_name_at(i, W, a, b):
    L = len(W)
    mod = L * L * L
    j = (a * i + b) % mod
    a1 = j % L
    a2 = (j // L) % L
    a3 = (j // (L * L)) % L
    return W[a1] + "-" + W[a2] + "-" + W[a3]


def generate_level(
    path, U, M, N, dur_lo, dur_hi, p0, max_step, base_ts, seed, firsts, lasts, words
):
    rng = random.Random(seed)
    W = words[:]
    rng.shuffle(W)
    full_names = pick_unique_full_names(U, firsts, lasts, rng)
    uid_set = set()
    user_ids = []
    emails_seen_local = {}
    user_map = {}
    for _ in range(len(full_names)):
        uid = rand_alnum8(rng)
        while uid in uid_set:
            uid = rand_alnum8(rng)
        uid_set.add(uid)
        user_ids.append(uid)
    for i, (f, l) in enumerate(full_names):
        e = email_from_name_alpha(f, l, emails_seen_local)
        w = rng.randint(0, 100)
        user_map[user_ids[i]] = (f + " " + l, e, w)
    L = len(W)
    if M > L * L:
        raise SystemExit(f"Not enough unique machine names for M={M}")
    if N > L * L * L:
        raise SystemExit(f"Not enough unique job names for N={N}")
    a2, b2 = lcg_params(L * L, rng)
    a3, b3 = lcg_params(L * L * L, rng)
    subs = gen_submission_times(N, base_ts, p0, max_step, rng)
    with open(path, "w", encoding="utf-8") as f:
        f.write(f"{len(full_names)} {M} {N}\n")
        for i, uid in enumerate(user_ids):
            nm, em, w = user_map[uid]
            f.write(f"{uid}\t{nm}\t{em}\t{w}\n")
        for i in range(M):
            name = pair_name_at(i, W, a2, b2)
            sp = binom_speed(rng)
            f.write(f"{name}\t{sp}\n")
        for k in range(N):
            nm = triple_name_at(k, W, a3, b3)
            uid = rng.choice(user_ids)
            ts = subs[k]
            dur = (int(rng.expovariate(0.1)) + 1) * 600
            f.write(f"{k+1}\t{nm}\t{uid}\t{ts}\t{dur}\n")


def main():
    outdir = sys.argv[1] if len(sys.argv) > 1 else "."
    seed = int(sys.argv[2]) if len(sys.argv) > 2 else 42
    first_file = sys.argv[3] if len(sys.argv) > 3 else "first_names.txt"
    last_file = sys.argv[4] if len(sys.argv) > 4 else "last_names.txt"
    wordlist_file = sys.argv[5] if len(sys.argv) > 5 else "words.txt"
    firsts = load_names(first_file)
    lasts = load_names(last_file)
    words = load_eff_words(wordlist_file)
    if not firsts or not lasts:
        raise SystemExit("first_names or last_names file is empty")
    base_ts = 1735689600
    small_Ns = [10, 20, 50, 100, 200, 300, 400, 500, 750, 1000]
    large_Ns = [
        2000,
        5000,
        10000,
        20000,
        50000,
        100000,
        200000,
        350000,
        500000,
        650000,
        800000,
        900000,
        950000,
        980000,
        1000000,
    ]
    levels = []
    for i in range(10):
        N = small_Ns[i]
        U = min(20 + i * 40, 10000)
        M = min(5 + i * 10, 9999)
        p0 = max(0.75 - i * 0.02, 0.55)
        step = 1 + i // 3
        levels.append((f"tests/input{str(i+1).zfill(2)}.txt", U, M, N, 1, 6, p0, step))
    for j in range(15):
        idx = 10 + j + 1
        N = large_Ns[j]
        U = min(800 + j * 650, 10000)
        M = min(100 + j * 300, 9999)
        p0 = max(0.5 - j * 0.02, 0.2)
        step = 2 + j // 3
        levels.append((f"tests/input{str(idx).zfill(2)}.txt", U, M, N, 1, 20, p0, step))
    os.makedirs(outdir, exist_ok=True)
    for i, (fname, U, M, N, dlo, dhi, p0, step) in enumerate(levels, start=1):
        path = os.path.join(outdir, fname)
        generate_level(
            path, U, M, N, dlo, dhi, p0, step, base_ts, seed + i, firsts, lasts, words
        )


if __name__ == "__main__":
    main()
