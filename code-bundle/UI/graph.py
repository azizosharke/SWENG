import matplotlib.pyplot as plt
import Data
import os


# Specify path tho results folder in testRunner
current_directory = os.path.dirname(__file__)
results_path = os.path.split(current_directory)
results_path = results_path[0]
results_path += "\\testRunner\\results"       # \\test\\hello.txt
path = True
# List files in directory
try:
    files = os.listdir(results_path)
except:
    print("ERROR: cannot determine path to " + results_path)
    path = False

if(path == True):
    # Check for valid result files
    valid = ".json"
    validFiles = []
    for file in files:
        if(valid in file):
            validFiles.append(file)

    data = Data.Data()
    for file in validFiles:
        fpath = [results_path, "\\", file]
        fpath = "".join(fpath)
        data.add(fpath)

    # If there are valid files in results directory
    if(len(validFiles) > 0):
        # PLOT AVG RUNTIME
        # x axis values (Filenames)
        x = validFiles

        # corresponding y axis values
        y = data.average_run

        # plotting the points
        plt.plot(x, y, color='green', linestyle='dashed', linewidth=2,
                 marker='o', markerfacecolor='blue', markersize=10)

        # setting x and y axis range
        plt.ylim(0, max(data.average_run) + 50)
        plt.xlim(-0.25, len(data.average_run) - 0.75)

        # naming the x axis
        plt.xlabel('Version')
        # naming the y axis
        plt.ylabel('Runtime')

        # giving a title to my graph
        plt.title('Average Runtime For Each Version')

        # function to show the plot
        plt.show()

        # PLOT Rest of data
        figure, axis = plt.subplots(2, 2)
        # For First result
        y1 = data.first_result
        axis[0, 0].plot(x, y1)
        axis[0, 0].set_title("First result")
        axis[0, 0].set_ylabel("Runtime")
        axis[0, 0].plot(x, y1, color='blue', linestyle='None', linewidth=2,
                        marker='o', markerfacecolor='blue', markersize=10)
        # For Final result
        y2 = data.final_result
        axis[0, 1].plot(x, y2)
        axis[0, 1].set_title("Final result")
        axis[0, 1].set_ylabel("Runtime")
        axis[0, 1].plot(x, y2, color='blue', linestyle='None', linewidth=2,
                        marker='o', markerfacecolor='blue', markersize=10)

        # For 99th Percentile
        y3 = data.p99
        axis[1, 0].plot(x, y3)
        axis[1, 0].set_title("99th Percentile")
        axis[1, 0].set_ylabel("Runtime")
        axis[1, 0].plot(x, y3, color='blue', linestyle='None', linewidth=2,
                        marker='o', markerfacecolor='blue', markersize=10)

        # For 50th Percntile
        y4 = data.p50
        axis[1, 1].plot(x, y4)
        axis[1, 1].set_title("50th Percentile")
        axis[1, 1].set_ylabel("Runtime")
        axis[1, 1].plot(x, y4, color='blue', linestyle='None', linewidth=2,
                        marker='o', markerfacecolor='blue', markersize=10)

        # Combine all the operations and display
        plt.show()
    else:
        print("ERROR: NO RESULT FILES DETECTED IN DIRECTORY")
else:
    print("Ensure results are written to " + results_path)
