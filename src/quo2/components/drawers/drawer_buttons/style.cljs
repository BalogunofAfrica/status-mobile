(ns quo2.components.drawers.drawer-buttons.style
  (:require [quo2.foundations.colors :as colors]))

(def outer-container
  {:height        216
   :opacity       1
   :border-radius 20})

(def blurred-button
  {:position                :absolute
   :top                     0
   :left                    0
   :right                   0
   :bottom                  0
   :opacity                 1
   :flex                    1
   :z-index                 1
   :border-top-left-radius  20
   :border-top-right-radius 20
   :background-color        :transparent
   :padding-vertical        12
   :padding-horizontal      20})

(def top-card
  {:flex                    1
   :border-top-left-radius  20
   :border-top-right-radius 20
   :overflow                :hidden})

(def bottom-card
  {:position                :absolute
   :top                     80
   :left                    0
   :right                   0
   :bottom                  0
   :border-top-left-radius  20
   :border-top-right-radius 20
   :overflow                :hidden})

(def bottom-container
  {:flex-direction  :row
   :justify-content :space-between})

(def bottom-icon
  {:border-radius   32
   :border-width    1
   :margin-left     24
   :height          32
   :width           32
   :justify-content :center
   :align-items     :center
   :border-color    colors/white-opa-5})

(def bottom-text
  {:flex  1
   :color colors/white-70-blur})

(def top-text
  {:color colors/white-70-blur})

(defn heading-text
  [gap]
  {:color         colors/white
   :margin-bottom gap})
