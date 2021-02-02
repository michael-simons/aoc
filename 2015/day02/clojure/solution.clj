(use '[clojure.string :only (trim-newline split-lines split)])

(def input 
    (map (fn [v] (sort (map bigint (split v #"x"))))
    (split-lines (trim-newline (slurp "input.txt")))))

(defn paper
  [dimensions]
  (let [[l w h] dimensions]
    (+ (* 3 l w) (* 2 l h) (* 2 w h))))

(defn ribbon  
  [dimensions]
  (let [[l w h] dimensions]
    (+ (* 2 l) (* 2 w) (* l w h))))

(println (reduce + (map paper input)))
(println (reduce + (map ribbon input)))
