(ns status-im2.contexts.shell.animation
  (:require [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.utils :as utils]
            [status-im2.contexts.shell.state :as state]
            [status-im2.contexts.shell.constants :as shell.constants]))

(defn open-home-stack
  [stack-id animate?]
  (let [home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (js/setTimeout
     (fn []
       (utils/load-stack stack-id)
       (utils/change-shell-status-bar-style))
     (if animate? shell.constants/shell-animation-time 0))))

(defn change-tab
  [stack-id]
  (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) false)
  (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
  (utils/load-stack stack-id)
  (utils/change-selected-stack-id stack-id true))

(defn bottom-tab-on-press
  [stack-id animate?]
  (when (and @state/shared-values-atom (not= stack-id @state/selected-stack-id))
    (if (utils/home-stack-open?)
      (change-tab stack-id)
      (open-home-stack stack-id animate?))
    (utils/update-view-id)))

(defn close-home-stack
  [animate?]
  (let [stack-id               nil
        home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) true)
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (utils/change-shell-status-bar-style)
    (when animate? (utils/update-view-id))))

;; Floating Screen

(defn animate-floating-screen
  [screen-id {:keys [id animation community-id hidden-screen? current-view-id]}]
  (when (utils/floating-screen-animate? screen-id animation)

    ;; Animate Floating Screen
    (reanimated/set-shared-value
     (get-in @state/shared-values-atom [screen-id :screen-state])
     animation)
    (reset! state/floating-screens-state
      (assoc @state/floating-screens-state screen-id animation))

    (if (utils/floating-screen-open? screen-id)

      ;; Events realted to opening of a screen
      (do
        (when-not (= current-view-id shell.constants/community-screen)
          (if community-id
            ;; When opening community chat, open community screen in background
            (js/setTimeout
             #(rf/dispatch [:shell/navigate-to shell.constants/community-screen
                            community-id shell.constants/open-screen-without-animation true])
             (* 2 shell.constants/shell-animation-time))
            ;; Otherwise open home screen in background
            (js/setTimeout
             #(open-home-stack
               (if (= screen-id shell.constants/community-screen)
                 :communities-stack
                 :chats-stack)
               false)
             (* 2 shell.constants/shell-animation-time))))
        (when-not hidden-screen?
          ;; Only update switcher cards for top screen
          (js/setTimeout
           #(rf/dispatch [:shell/add-switcher-card screen-id id])
           (* 4 shell.constants/shell-animation-time))))

      ;; Events realted to closing of a screen
      (when (= screen-id shell.constants/chat-screen)
        (js/setTimeout
         #(rf/dispatch [:chat/close])
         shell.constants/shell-animation-time)))))

(defn set-floating-screen-position
  [left top card-type]
  (let [screen-id (case card-type
                    (shell.constants/one-to-one-chat-card
                     shell.constants/private-group-chat-card
                     shell.constants/community-channel-card)
                    shell.constants/chat-screen

                    shell.constants/community-card
                    shell.constants/community-screen

                    nil)]
    (when screen-id
      (reanimated/set-shared-value
       (get-in @state/shared-values-atom [screen-id :screen-left])
       left)
      (reanimated/set-shared-value
       (get-in @state/shared-values-atom [screen-id :screen-top])
       top))))
