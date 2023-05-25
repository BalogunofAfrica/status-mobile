(ns status-im2.common.wizard.view
  (:require [react-native.core :as rn]
            [oops.core :as oops]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]))

(def slide-away-ms 10)
(def slide-up-ms 5)

(def slide-to-initial-ms 5)

(def swap-content-ms 100)

(defn next-step-animation [translateX translateY opacity window-width window-height]
  (reanimated/set-shared-value
   translateX
   (reanimated/with-sequence
     (reanimated/with-timing  (- (* 2   window-width))
       {:duration slide-away-ms
        :easing (:easing1 reanimated/easings)})


       (reanimated/with-timing  (* 2 window-width)
       {:duration slide-to-initial-ms
        :easing (:easing1 reanimated/easings)})

     (reanimated/with-timing 0
       {:duration 25
        :easing (:easing1 reanimated/easings)})))


  (reanimated/set-shared-value
   translateY
   (reanimated/with-sequence
     (reanimated/with-delay swap-content-ms
       (reanimated/with-timing (* 2 window-height)
                                            {:duration slide-up-ms
                                             :easing (:easing1 reanimated/easings)}))

     (reanimated/with-timing 0
       {:duration 10
        :easing (:easing1 reanimated/easings)})))

  ;; (reanimated/set-shared-value
  ;;  opacity
  ;;  (reanimated/with-sequence
  ;;    (reanimated/with-timing  0
  ;;      {:duration 25
  ;;       :easing (:easing1 reanimated/easings)})

  ;;    (reanimated/with-delay 50
  ;;      (reanimated/with-timing  1
  ;;        {:duration 200
  ;;         :easing (:easing1 reanimated/easings)}))))
  )

(defn prev-step-animation [shared-val opacity window-width]
  (reanimated/set-shared-value
   shared-val
   (reanimated/with-sequence
     (reanimated/with-timing  (+ (* 2   window-width))
       {:duration 25
        :easing (:easing1 reanimated/easings)})

     (reanimated/with-timing  (- (* 2 window-width))
       {:duration 5
        :easing (:easing1 reanimated/easings)})

     (reanimated/with-timing  0
       {:duration 25
        :easing (:easing1 reanimated/easings)})))

  (reanimated/set-shared-value
   opacity
   (reanimated/with-sequence
     (reanimated/with-timing  0
       {:duration 25
        :easing (:easing1 reanimated/easings)})

     (reanimated/with-delay 150
       (reanimated/with-timing  1
         {:duration 200
          :easing (:easing1 reanimated/easings)})))))



(defn f-step
  [{:keys [content
           animation
           duration
           translateX
           translateY
           opacity
           current-step
           prev-step
           use-native-driver
           content-container-style]}]
  (let [style (atom 0)
        {window-width :width}   (rn/get-window)
        ;; window-width 50
        style2 (reanimated/use-shared-value 0)
        style3 (reanimated/use-shared-value 0)
        style4 (reanimated/use-shared-value 0)
        left window-width]


    ;; (rn/use-effect (fn []
    ;;                  (when  (or (and (zero? prev-step) (zero? current-step)) (> current-step prev-step))

    ;;                    (reanimated/set-shared-value
    ;;                     s
    ;;                     (reanimated/with-timing (- window-width)
    ;;                       {:duration 100
    ;;                        :easing (:easing1 reanimated/easings)})))

    ;;                  (when (and prev-step (< current-step prev-step))

    ;;                    (reanimated/set-shared-value
    ;;                     s
    ;;                     (reanimated/with-timing 0
    ;;                       {:duration 100
    ;;                        :easing (:easing1 reanimated/easings)}))))
    ;;                [prev-step current-step])
    ;; (rn/use-effect
    ;;  (fn []
    ;;    (when-not (zero? current-step)

    ;; (when (< current-step prev-step)
    ;;   (js/console.log "prev" current-step prev-step)
    ;;   (reanimated/set-shared-value
    ;;    style3
    ;;    (reanimated/with-timing  window-width
    ;;      {:duration 1500
    ;;       :easing (:easing1 reanimated/easings)})))

    ;;      (when (> current-step prev-step)
    ;;        (js/console.log "next" current-step prev-step)

    ;;        (reanimated/set-shared-value
    ;;         style3
    ;;         (reanimated/with-delay 200
    ;;           (reanimated/with-timing  (- (* 2 60))
    ;;             {:duration 1500
    ;;              :easing (:easing1 reanimated/easings)}))))

    ;;      ))
    ;;  [current-step prev-step])

    [reanimated/view {:style (reanimated/apply-animations-to-style
                              {:transform [{:translateX translateX}
                                           {:translateY translateY}]
                               :opacity opacity}
                              {:position :absolute
                               :top      0
                               :bottom   0
                               :left     0
                               :right     0})} content]
  ;; [rn/view {:style content-container-style} content]
    ))

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn f-view [{:keys [steps active-step current-step
                      on-next on-prev first-step? last-step?
                      duration wizard-ref]}]
  (let [active-step-no (reagent/atom 0)
        prev-step-no (reagent/atom 0)
        {window-width :width window-height :height}   (rn/get-window)
        next? (reagent/atom true)]
    (fn []
      (let [translateX (reanimated/use-shared-value 0)
            translateY (reanimated/use-shared-value 0)
            opacity (reanimated/use-shared-value 1)]
        (oops/oset! wizard-ref "current"
                    {:next (fn []
                             (when (not= (- (count steps) 1) @active-step-no)
                               (next-step-animation translateX translateY opacity window-width window-height)
                               (js/setTimeout (fn []
                                                (reset! prev-step-no @active-step-no)
                                                (swap! active-step-no inc)
                                                (reset! next? true)) swap-content-ms))
                             (when current-step
                               (current-step {:current-step @active-step-no
                                              :first-step? (zero? @active-step-no)
                                              :last-step? (= @active-step-no (- (count steps) 1))})))

                     :prev (fn []
                             (when-not (zero? @active-step-no)
                               (prev-step-animation translateX opacity window-width)
                               (js/setTimeout (fn []
                                                (reset! prev-step-no @active-step-no)
                                                (swap! active-step-no dec)
                                                (reset! next? true)) 50))
                             (when current-step
                               (current-step {:current-step @active-step-no
                                              :first-step? (zero? @active-step-no)
                                              :last-step? (= @active-step-no (- (count steps) 1))})))})
        (rn/use-effect (fn []
                         (when current-step (current-step
                                             {:current-step @active-step-no
                                              :first-step? (zero? @active-step-no)
                                              :last-step? (= @active-step-no (- (count steps) 1))})))
                       [active-step-no (count steps)])

        (rn/use-effect (fn []
                         (when first-step? (first-step? (zero? @active-step-no)))
                         (when last-step? (last-step? (= @active-step-no (- (count steps) 1)))))
                       [active-step-no (count steps)])
        [:f> f-step {:content-container-style page-container
                     :current-step @active-step-no
                     :prev-step @prev-step-no
                     :translateX translateX
                     :translateY translateY
                     :opacity opacity
                     :animation (:animation (nth steps @active-step-no))
                     :content (:content (nth steps @active-step-no))}]))))

(defn view [props]
  [:f> f-view props])

