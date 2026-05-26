from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
TEXT_SUFFIXES = {
    ".css",
    ".fxml",
    ".java",
    ".md",
    ".properties",
    ".sql",
    ".txt",
    ".xml",
}
SKIP_DIRS = {".git", ".idea", ".vscode", "target"}

MOJIBAKE_RE = re.compile(
    r"(?:\u00c3[\u0080-\u00bf\u00c0-\u00ff])"
    r"|(?:\u00e1[\u00ba\u00bb][\u0080-\u00bf\u00c0-\u00ff]?)"
    r"|(?:\u00c4[\u0080-\u00bf\u00c0-\u00ff])"
    r"|(?:\u00c6[\u0080-\u00bf\u00c0-\u00ff])"
    r"|[\u0080-\u009f\ufffd]"
)


def iter_text_files():
    for path in ROOT.rglob("*"):
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        if path.is_file() and path.suffix.lower() in TEXT_SUFFIXES:
            yield path


def main():
    problems = []
    for path in iter_text_files():
        text = path.read_text(encoding="utf-8-sig", errors="replace")
        for line_no, line in enumerate(text.splitlines(), start=1):
            if MOJIBAKE_RE.search(line):
                preview = line.strip().encode("unicode_escape").decode("ascii")
                problems.append((path.relative_to(ROOT), line_no, preview))

    if problems:
        print("Found possible Vietnamese mojibake text:")
        for path, line_no, preview in problems:
            print(f"{path}:{line_no}: {preview}")
        return 1

    print("Vietnamese text check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
