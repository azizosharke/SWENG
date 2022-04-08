class Data:
    def __init__(self):
        self.first_result = []
        self.final_result = []
        self.average_run = []
        self.p99 = []
        self.p50 = []


    def add(self, Filename):
        f = open(Filename, 'r')  # read file
        # get data from each file
        # read data line by line
        f.__next__()
        line = f.readline().split(": ")
        line = line[1].split(",")
        first_result = float(line[0])
        line = f.readline().split(": ")
        line = line[1].split(",")
        final_result = float(line[0])
        line = f.readline().split(": ")
        line = line[1].split(",")
        average_run = float(line[0])
        line = f.readline().split(": ")
        line = line[1].split(",")
        p99 = float(line[0])
        line = f.readline().split(": ")
        line = line[1].split(",")
        p50 = float(line[0])

        self.first_result.append(first_result)
        self.final_result.append(final_result)
        self.average_run.append(average_run)
        self.p99.append(p99)
        self.p50.append(p50)

        f.close()

