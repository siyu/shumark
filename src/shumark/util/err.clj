(ns shumark.util.err)

(defmacro with-dflt
  "Evaluate the body and in case of exception return the default value."
  [dflt & body]
  `(try
     (do ~@body)
     (catch Exception _# ~dflt)))