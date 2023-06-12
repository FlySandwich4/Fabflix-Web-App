file = "/Users/FlySandwich/Desktop/ts_tj.txt"

def calculate_average(filename):
    ts_total = 0
    ts_num = 0
    tj_total = 0
    tj_num = 0

    with open(filename, 'r') as file:
        lines = file.readlines()

    index = 0
    while index < len(lines):
        line = lines[index].strip()
        if line.startswith("Query:"):
            try:
                ts_line = lines[index + 1].strip().split(':')
                tj_line = lines[index + 2].strip().split(':')

                ts_value = float(ts_line[1].strip())
                ts_total += ts_value
                ts_num += 1

                tj_value = float(tj_line[1].strip())
                tj_total += tj_value
                tj_num += 1

            except (ValueError, IndexError):
                print(f"Ignoring invalid lines at index {index}, {index + 1}, and {index + 2}")

        index += 3

    if ts_num > 0:
        ts_average = ts_total / ts_num
    else:
        ts_average = 0

    if tj_num > 0:
        tj_average = tj_total / tj_num
    else:
        tj_average = 0

    return ts_average, tj_average

ts_average, tj_average = calculate_average(file)
print(f"TS average: {ts_average}")
print(f"TJ average: {tj_average}")

