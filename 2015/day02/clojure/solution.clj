(use '[clojure.string :only (trim-newline split-lines split)])

(def input 
    "I use again slurp to read the file and two library functions
     to trim the newlines and split the whole thing into a list. 
     An anonymous function is used on each line to split line by 
     the letter `x` and map the values to an int. Those ints are than
     sorted and the variable `input` will be a lazy list of int arrays."
    (map (fn [v] (sort (map bigint (split v #"x"))))
    (split-lines (trim-newline (slurp "input.txt")))))

(defn paper
    "As I know that the array is sorted, I can deconstruct it into the 
     3 values contained. The riddle for the paper is that the smallest area 
     is in there 3 and not 2 times. As the smallest area is defined by the first
     two elements, we multiple them 3 instead of 2 times like the rest."
  [dimensions]
  (let [[l w h] dimensions]
    (+ (* 3 l w) (* 2 l h) (* 2 w h))))

(defn ribbon  
    "Same idea as above: The smallest perimeter is defined by the 2 smallest values.
     The volume is of course the product of all 3."
  [dimensions]
  (let [[l w h] dimensions]
    (+ (* 2 l) (* 2 w) (* l w h))))

(println (reduce + (map paper input)))
(println (reduce + (map ribbon input)))
