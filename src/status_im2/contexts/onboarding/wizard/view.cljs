(ns status-im2.contexts.onboarding.wizard.view
  (:require
;;    ["react-native-wizard" :default Wizard]
   [status-im2.common.wizard.view :as wizard]
   [quo2.core :as quo]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [react-native.safe-area :as safe-area]
   [status-im2.contexts.onboarding.create-profile.view :as create-profile]
   [status-im2.contexts.onboarding.create-password.view :as create-password]
   [status-im2.contexts.onboarding.common.background.view :as background]
   [status-im2.contexts.onboarding.enable-biometrics.view :as biometrics]
   ))

;; (def wizard (reagent/adapt-react-class Wizard))
;; (def wizard-ref (reagent/adapt-react-class (.-WizardRef Wizard)))


;; const wizard = useRef<WizardRef>(null);
;; const [isFirstStep, setIsFirstStep] = useState()
;; const [isLastStep, setIsLastStep] = useState()
;; const stepList = [
;;     {
;;       content: <Image source={{uri: "http://placehold.it/96x96"}} style={{width:50, height:50}}/>,
;;     },
;;     {
;;       content: <Step2 testProp="Welcome to Second Step"/>
;;     },
;;     {
;;       content: <Step3 step3Prop="Welcome to Third Step"/>
;;     },


(defn f-view []
  (let [insets                    (safe-area/get-insets)
        wizard-ref (rn/use-ref nil)
        first-step? (reagent/atom true)
        last-step? (reagent/atom true)
        steplist [{:content [create-profile/create-profile {:wizard-ref wizard-ref}]}
                  {:content  [create-password/create-password {:wizard-ref wizard-ref}]}
                  {:content  [biometrics/enable-biometrics {:wizard-ref wizard-ref}]}

                  
                ;;   {:content [quo/text {} "Page5"]}
                  ]]
    [rn/view {:position :absolute
              :top 0
              :left 0
              :right 0
              :bottom 0

              :padding-top (:top insets)}
     [wizard/view
      {:wizard-ref wizard-ref
       :next-step-animation "slideRight"
       :prev-step-animation "slideLeft"
       :active-step 0
       :steps steplist

    ;;    :on-next #(js/alert "hello")}
       }
      [rn/view {:flex 1}
    ;;   (map (fn [item]
    ;;          []
    ;;          ) steplist)
       ]]]))

(defn view [props]
  [:<>
   [background/view true]
   [:f> f-view props]])
