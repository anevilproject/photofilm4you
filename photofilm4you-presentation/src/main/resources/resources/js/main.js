{
    // Tooltip Initialization
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    })

    $(document).ready(function () {
        window.addEventListener('scroll', checkPosition);
        checkPosition();
    })

    function checkPosition() {
        const elements = document.querySelectorAll('.hidden');
        const windowHeight = window.innerHeight;
        let firstSeq = undefined;

        for (let i = 0; i < elements.length; i++) {
            let element = elements[i];
            let positionFromTop = elements[i].getBoundingClientRect().top;
            let delay = 80;

            if (positionFromTop - windowHeight <= 0) {
                if (element.dataset.seq) {
                    let seq = parseInt(element.dataset.seq);

                    if (firstSeq === undefined) {
                        firstSeq = seq;
                        seq = 0;
                    } else {
                        seq = seq - firstSeq;
                    }
                    delay = delay * seq;
                }

                setTimeout(function () {
                    element.classList.add('fade-in-element');
                    element.classList.remove('hidden');
                }, delay);
            }
        }
    }
}

