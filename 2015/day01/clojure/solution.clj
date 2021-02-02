(def input (clojure.string/trim-newline (slurp "input.txt")))

(def freq (frequencies input))
(def starOne
    (- (get freq \() (get freq \))))
(println starOne)

(def starTwo
    (count
        (take-while (fn [p] (not= p -1))
            (reductions (fn [sum num] (+ sum num)) 0 (map {\( 1 \) -1} input)))))
(println starTwo)
