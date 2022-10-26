(ns status-im.ui2.screens.chat.components.received-cr-item
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> e62e73350... feat: messages contact requests
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.foundations.typography :as typography]
            [clojure.string :as str]
            [status-im.utils.utils :as utils.utils]
            [status-im.utils.datetime :as time]
<<<<<<< HEAD
<<<<<<< HEAD
            [status-im.i18n.i18n :as i18n]
            [quo2.components.notifications.notification-dot :refer [notification-dot]]))

<<<<<<< HEAD:src/quo2/components/list_items/received_cr_item.cljs
(defn received-cr-item [{:keys [chat-id message timestamp read]}]
=======

(defn received-cr-item [{:keys [chat-id image contact message timestamp read]}]
>>>>>>> 7f132b433... feat: messages home items:src/quo2/components/list_items/received_contact_request.cljs
  (let [no-ens-name  (str/blank? (get-in message [:content :ens-name]))
        display-name (first (<sub [:contacts/contact-two-names-by-identity chat-id]))]
    [rn/view {:style {:flex-direction :row
                      :padding-top    8
                      :margin-top     4
                      :padding-bottom 12
                      :flex           1}}
     (when-not read
       [notification-dot {:right 32 :top 16}])
     [user-avatar/user-avatar {:full-name         display-name
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :ring?             false}]
     [rn/view {:style {:margin-horizontal 8}}
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/font-semi-bold typography/paragraph-1
                               {:color        (colors/theme-colors colors/neutral-100 colors/white)
                                :margin-right 8})} display-name]
       (when no-ens-name [rn/text {:style (merge typography/font-regular typography/label
                                                 {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                  :margin-top 4})}
                          (str (utils.utils/get-shortened-address chat-id) " · ")])
       [rn/text {:style (merge typography/font-regular typography/label
                               {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                :margin-top 4})}
        (time/to-short-str timestamp)]]
      [rn/view {:style {:border-radius      12
                        :margin-top         10
                        :padding-horizontal 12
                        :padding-vertical   8
                        :border-width       1
                        :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-70)}}
       [rn/text {:style (merge typography/font-regular
                               typography/paragraph-1
                               {:color (colors/theme-colors colors/neutral-100 colors/white)})}
        (:text (:content message))]]
      [rn/view {:style {:margin-top     12
                        :flex-direction :row}}
       [rn/touchable-opacity {:accessibility-label :decline-cr
                              :on-press            #(>evt [:contact-requests.ui/decline-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/danger-50 colors/danger-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8}}
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color colors/white})} (i18n/label :t/decline)]]
       [rn/touchable-opacity {:accessibility-label :accept-cr
                              :on-press            #(>evt [:contact-requests.ui/accept-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/success-50 colors/success-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8
                                                    :margin-left        8}}
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color colors/white})} (i18n/label :t/accept)]]]]]))
<<<<<<< HEAD:src/quo2/components/list_items/received_cr_item.cljs


=======
=======
            [quo2.components.counter.counter :as quo2.counter]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.avatars.channel-avatar :as channel-avatar]
            [quo2.components.markdown.text :as quo2.text]
            [quo.theme :as theme]))
=======
            [status-im.i18n.i18n :as i18n]))
>>>>>>> e62e73350... feat: messages contact requests
=======
            [status-im.i18n.i18n :as i18n]
            [quo2.components.notifications.notification-dot :refer [notification-dot]]))
>>>>>>> 44c8c1017... refactor

(defn get-display-name [chat-id no-ens-name no-nickname]
  (let [name (first (<sub [:contacts/contact-two-names-by-identity chat-id]))]
    (if (and no-ens-name no-nickname)
      (let [[word1 word2] (str/split name " ")]
        (str word1 " " word2))
      name)))

<<<<<<< HEAD
<<<<<<< HEAD
(defn list-item [item]
  (println "itexs" item)
  [rn/view
   [rn/text "SOME ITEM"]])
>>>>>>> cfa806fc3... contact requests
<<<<<<< HEAD:src/quo2/components/list_items/received_cr_item.cljs
>>>>>>> 2d3d83283... contact requests:src/quo2/components/list_items/received_contact_request.cljs
=======
=======
(defn list-item [{:keys [chat-id image contact message timestamp]}]
  (let [no-ens-name  (str/blank? (:ens-name (:content message)))
        no-nickname  (if (= (:nickname (:names contact)) nil) true false)
=======
(defn list-item [{:keys [chat-id image contact message timestamp read]}]
  (let [no-ens-name  (str/blank? (get-in message [:content :ens-name]))
        no-nickname  (nil? (get-in contact [:names :nickname]))
>>>>>>> 44c8c1017... refactor
        display-name (get-display-name chat-id no-ens-name no-nickname)]
    [rn/view {:style {:flex-direction :row
                      :padding-top    8
                      :margin-top     4
                      :padding-bottom 12
                      :flex           1}}
     (when-not read
       [notification-dot {:right 32 :top 16}])
     [user-avatar/user-avatar {:full-name         display-name
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :profile-picture   image
                               :ring?             false}]
     [rn/view {:style {:margin-horizontal 8}}
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/font-semi-bold typography/paragraph-1
                               {:color        (colors/theme-colors :black :white)
                                :margin-right 8})} display-name]
       (when no-ens-name [rn/text {:style (merge typography/font-regular typography/label
                                                 {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                  :margin-top 4})}
                          (str (utils.utils/get-shortened-address chat-id) " · ")])
       [rn/text {:style (merge typography/font-regular typography/label
                               {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                :margin-top 4})}
        (time/to-short-str timestamp)]]
      [rn/view {:style {:border-radius      12
                        :margin-top         10
                        :padding-horizontal 12
                        :padding-vertical   8
                        :border-width       1
                        :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-70)}}
       [rn/text {:style (merge typography/font-regular
                               typography/paragraph-1
                               {:color (colors/theme-colors :black :white)})}
        (:text (:content message))]]
      [rn/view {:style {:margin-top     12
                        :flex-direction :row}}
       [rn/touchable-opacity {:accessibility-label :decline-cr
                              :on-press            #(>evt [:contact-requests.ui/decline-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/danger-50 colors/danger-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8}}
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color :white})} (i18n/label :t/decline)]]
       [rn/touchable-opacity {:accessibility-label :accept-cr
                              :on-press            #(>evt [:contact-requests.ui/accept-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/success-50 colors/success-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8
                                                    :margin-left        8}}
<<<<<<< HEAD
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color "#ffffff"})} (i18n/label :t/accept)]]]]]))
>>>>>>> e62e73350... feat: messages contact requests
<<<<<<< HEAD:src/quo2/components/list_items/received_cr_item.cljs
>>>>>>> 6eabaa136... feat: messages contact requests:src/quo2/components/list_items/received_contact_request.cljs
=======
=======
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color :white})} (i18n/label :t/accept)]]]]]))
>>>>>>> 44c8c1017... refactor
>>>>>>> 243fb7076... refactor:src/quo2/components/list_items/received_contact_request.cljs