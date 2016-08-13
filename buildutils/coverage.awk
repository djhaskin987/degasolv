#!/bin/awk -f
BEGIN {
    getline;
    uncovered_lines=0; }
// {
    uncovered_lines = uncovered_lines + $3 - $4;
}
END {
    if (uncovered_lines > 0)
    {
        print "Number of un-covered lines: " uncovered_lines
        exit 1
    }
    else
    {
        print "All lines covered."
        exit 0
    }
}
