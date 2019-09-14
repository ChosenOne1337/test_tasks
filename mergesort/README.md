# Files merge sort
  A small utility that merges several sorted files into one sorted file. 
  
  Two types of files are supported - containing strings or numbers.
  
  Files can be sorted both in ascending and in descending order.

## Installation

* Download [this](https://drive.google.com/file/d/1-g8bWcZzDS-QgFhlw-bYqg52DUzJZLHk/view?usp=sharing) jar file
* Or build your own from the sources with ***gradle*** (dependencies are specified in the "*build.gradle*" file)

## Usage
  Run the jar file with ***java*** (Java 11 or newer is required):
  
  ```
  java -jar <jarfile>.jar [options] <output_file> [<input_file_1>, ...]
  ```
  
  The first file in the list is considered as an output file, the remaining - as input files.
  
  Specification of an output file name is obligatory. Also at least one input file has to be specified.
  
## Options
* **-a**, **-d**: sort in ascending/descending order, respectively (ascending order is used by default)
* **-s**, **-i**: specify that input files contain strings/numbers (one of them is required)
* **-h** or **--help**: show usage info

## Implementation features
Partially sorted files are allowed, out-of-order lines are just skipped.

A file can also contain lines incompatible with the file type specified in the options 
(for example, non-digit character sequence in a number file). Such lines are skipped.

The process of files merger is broken into subtasks, which merge two different files into one.
Such subtasks are passed to a thread pool, where they are executed independently.
When the required number of mergers has occurred (which is **N - 1**, where **N** is the initial number of input files),
the program finishes.

All the files pending merger, including the temporary ones 
(which are created in the process of mergers), are kept sorted by their length.
It allows to choose two shortest files available at the moment for further merger. 
This can minimize the total number of lines written to files during all the mergers.
